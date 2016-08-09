/*
 * Copyright (c) 2003 Constantin S. Svintsoff <kostik@iclub.nsu.ru>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The names of the authors may not be used to endorse or promote
 *    products derived from this software without specific prior written
 *    permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

#include "readlink.h"

#include <string>

#include <errno.h>
#include <sys/param.h>
#include <sys/stat.h>
#include <unistd.h>

#ifdef MOE_WINDOWS
#include <winnt.h>
#include <winioctl.h>
#endif

/**
 * This differs from realpath(3) mainly in its behavior when a path element does not exist or can
 * not be searched. realpath(3) treats that as an error and gives up, but we have Java-compatible
 * behavior where we just assume the path element was not a symbolic link. This leads to a textual
 * treatment of ".." from that point in the path, which may actually lead us back to a path we
 * can resolve (as in "/tmp/does-not-exist/../blah.txt" which would be an error for realpath(3)
 * but "/tmp/blah.txt" under the traditional Java interpretation).
 *
 * This implementation also removes all the fixed-length buffers of the C original.
 */
bool canonicalize_path(const char* path, std::string& resolved) {
    // 'path' must be an absolute path.
#ifndef MOE_WINDOWS
    if (path[0] != '/') {
#else
    if (!isalpha(path[0]) || path[1] != ':' || path[2] != '\\') {
#endif
        errno = EINVAL;
        return false;
    }

#ifndef MOE_WINDOWS
    resolved = "/";
    if (path[1] == '\0') {
        return true;
    }
#else
    resolved = std::string("") + path[0] + ":\\";
    if (path[3] == '\0') {
        return true;
    }
#endif

    // Iterate over path components in 'left'.
    int symlinkCount = 0;
#ifndef MOE_WINDOWS
    std::string left(path + 1);
#else
    std::string left(path + 3);
#endif
    while (!left.empty()) {
        // Extract the next path component.
#ifndef MOE_WINDOWS
        size_t nextSlash = left.find('/');
#else
        size_t nextSlash = left.find('\\');
#endif
        std::string nextPathComponent = left.substr(0, nextSlash);
        if (nextSlash != std::string::npos) {
            left.erase(0, nextSlash + 1);
        } else {
            left.clear();
        }
        if (nextPathComponent.empty()) {
            continue;
        } else if (nextPathComponent == ".") {
            continue;
        } else if (nextPathComponent == "..") {
            // Strip the last path component except when we have single "/".
            if (resolved.size() > 1) {
#ifndef MOE_WINDOWS
                resolved.erase(resolved.rfind('/'));
#else
                resolved.erase(resolved.rfind('\\'));
#endif
            }
            continue;
        }

        // Append the next path component.
#ifndef MOE_WINDOWS
        if (resolved[resolved.size() - 1] != '/') {
            resolved += '/';
        }
#else
        if (resolved[resolved.size() - 1] != '\\') {
            resolved += '\\';
        }
#endif
        resolved += nextPathComponent;

        // See if we've got a symbolic link, and resolve it if so.
#ifndef MOE_WINDOWS
        struct stat sb;
        if (lstat(resolved.c_str(), &sb) == 0 && S_ISLNK(sb.st_mode)) {
#else
        bool isSymLink = false;
        {
            DWORD dwAttr = ::GetFileAttributesA(path);
            if (dwAttr != -1 && (dwAttr & FILE_ATTRIBUTE_REPARSE_POINT) != 0) {
                HANDLE hToken;
                TOKEN_PRIVILEGES tp;
                ::OpenProcessToken(::GetCurrentProcess(), TOKEN_ADJUST_PRIVILEGES, &hToken);
                ::LookupPrivilegeValue(NULL, SE_BACKUP_NAME, &tp.Privileges[0].Luid);
                tp.PrivilegeCount = 1;
                tp.Privileges[0].Attributes = SE_PRIVILEGE_ENABLED;
                ::AdjustTokenPrivileges(hToken, FALSE, &tp, sizeof(TOKEN_PRIVILEGES), NULL, NULL);
                ::CloseHandle(hToken);
                HANDLE handle = ::CreateFileA(path, GENERIC_READ, 0, NULL, OPEN_EXISTING, FILE_FLAG_OPEN_REPARSE_POINT | FILE_FLAG_BACKUP_SEMANTICS, NULL);
                if (handle != INVALID_HANDLE_VALUE) {
                    BYTE buf[MAXIMUM_REPARSE_DATA_BUFFER_SIZE];
                    REPARSE_DATA_BUFFER& ReparseBuffer = (REPARSE_DATA_BUFFER&)buf;
                    DWORD dwRet;
                    if (::DeviceIoControl(handle, FSCTL_GET_REPARSE_POINT, NULL, 0, &ReparseBuffer,
                        MAXIMUM_REPARSE_DATA_BUFFER_SIZE, &dwRet, NULL)) {
                        isSymLink = ReparseBuffer.ReparseTag == IO_REPARSE_TAG_SYMLINK;
                    }
                    ::CloseHandle(handle);
                }
            }
        }
        if (isSymLink) {
#endif
            if (symlinkCount++ > MAXSYMLINKS) {
                errno = ELOOP;
                return false;
            }

            std::string symlink;
            if (!readlink(resolved.c_str(), symlink)) {
                return false;
            }
#ifndef MOE_WINDOWS
            if (symlink[0] == '/') {
#else
            if (isalpha(symlink[0]) && symlink[1] == ':' && symlink[2] == '\\') {
#endif
                // The symbolic link is absolute, so we need to start from scratch.
#ifndef MOE_WINDOWS
                resolved = "/";
#else
                resolved = std::string("") + symlink[0] + ":\\";
                symlink = symlink.substr(2);
#endif
            } else if (resolved.size() > 1) {
                // The symbolic link is relative, so we just lose the last path component (which
                // was the link).
#ifndef MOE_WINDOWS
                resolved.erase(resolved.rfind('/'));
#else
                resolved.erase(resolved.rfind('\\'));
#endif
            }

            if (!left.empty()) {
#ifndef MOE_WINDOWS
                const char* maybeSlash = (symlink[symlink.size() - 1] != '/') ? "/" : "";
#else
                const char* maybeSlash = (symlink[symlink.size() - 1] != '\\') ? "\\" : "";
#endif
                left = symlink + maybeSlash + left;
            } else {
                left = symlink;
            }
        }
    }

    // Remove trailing slash except when the resolved pathname is a single "/".
#ifndef MOE_WINDOWS
    if (resolved.size() > 1 && resolved[resolved.size() - 1] == '/') {
#else
    if (resolved.size() > 1 && resolved[resolved.size() - 1] == '\\') {
#endif
        resolved.erase(resolved.size() - 1, 1);
    }
    return true;
}
