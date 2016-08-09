/*
 * Copyright (c) 1995, 2003, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * Posix-compatible directory access routines
 */

#include <windows.h>
#include <direct.h>                    /* For _getdrive() */
#include <errno.h>
#ifdef MOE_WINDOWS
#pragma push_macro("NDEBUG")
#undef NDEBUG
#endif
#include <assert.h>
#ifdef MOE_WINDOWS
#pragma pop_macro("NDEBUG")
#endif

#include "dirent_md.h"


/* Caller must have already run dirname through JVM_NativePath, which removes
   duplicate slashes and converts all instances of '/' into '\\'. */

DIR *
opendir(const char *dirname)
{
#ifndef MOE_WINDOWS
    DIR *dirp = (DIR *)malloc(sizeof(DIR));
#endif
    DWORD fattr;
    char alt_dirname[4] = { 0, 0, 0, 0 };

#ifndef MOE_WINDOWS
    if (dirp == 0) {
        errno = ENOMEM;
        return 0;
    }
#endif

    /*
     * Win32 accepts "\" in its POSIX stat(), but refuses to treat it
     * as a directory in FindFirstFile().  We detect this case here and
     * prepend the current drive name.
     */
    if (dirname[1] == '\0' && dirname[0] == '\\') {
        alt_dirname[0] = _getdrive() + 'A' - 1;
        alt_dirname[1] = ':';
        alt_dirname[2] = '\\';
        alt_dirname[3] = '\0';
        dirname = alt_dirname;
    }

#ifndef MOE_WINDOWS
    dirp->path = (char *)malloc(strlen(dirname) + 5);
    if (dirp->path == 0) {
        free(dirp);
        errno = ENOMEM;
        return 0;
    }
#else
    DIR *dirp = (DIR *)malloc(sizeof(DIR) + strlen(dirname) + 5 - 1);
    if (dirp == 0) {
        errno = ENOMEM;
        return 0;
    }
#endif
#ifdef MOE_WINDOWS
#define path dd_name
#define handle dd_handle
#define find_data dd_dta
#define FindFirstFile _findfirst
#endif
    strcpy(dirp->path, dirname);

    fattr = GetFileAttributes(dirp->path);
    if (fattr == ((DWORD)-1)) {
#ifndef MOE_WINDOWS
        free(dirp->path);
#endif
        free(dirp);
        errno = ENOENT;
        return 0;
    } else if ((fattr & FILE_ATTRIBUTE_DIRECTORY) == 0) {
#ifndef MOE_WINDOWS
        free(dirp->path);
#endif
        free(dirp);
        errno = ENOTDIR;
        return 0;
    }

    /* Append "*.*", or possibly "\\*.*", to path */
    if (dirp->path[1] == ':'
        && (dirp->path[2] == '\0'
            || (dirp->path[2] == '\\' && dirp->path[3] == '\0'))) {
        /* No '\\' needed for cases like "Z:" or "Z:\" */
        strcat(dirp->path, "*.*");
    } else {
        strcat(dirp->path, "\\*.*");
    }

    dirp->handle = FindFirstFile(dirp->path, &dirp->find_data);
    if (dirp->handle == INVALID_HANDLE_VALUE) {
        if (GetLastError() != ERROR_FILE_NOT_FOUND) {
#ifndef MOE_WINDOWS
            free(dirp->path);
#endif
            free(dirp);
            errno = EACCES;
            return 0;
        }
    }
    return dirp;
#ifdef MOE_WINDOWS
#undef path
#undef handle
#undef find_data
#undef FindFirstFile
#endif
}

struct dirent *
readdir(DIR *dirp)
{
#ifdef MOE_WINDOWS
#define dirent dd_dir
#define handle dd_handle
#define find_data dd_dta
#define cFileName name
#define FindNextFile(h, d) (_findnext(h, d) == 0)
#endif
    if (dirp->handle == INVALID_HANDLE_VALUE) {
        return 0;
    }

    strcpy(dirp->dirent.d_name, dirp->find_data.cFileName);

    if (!FindNextFile(dirp->handle, &dirp->find_data)) {
        if (GetLastError() == ERROR_INVALID_HANDLE) {
            errno = EBADF;
            return 0;
        }
        FindClose(dirp->handle);
        dirp->handle = INVALID_HANDLE_VALUE;
    }

    return &dirp->dirent;
#ifdef MOE_WINDOWS
#undef dirent
#undef handle
#undef find_data
#undef cFileName
#undef FindNextFile
#endif
}

int
closedir(DIR *dirp)
{
#ifdef MOE_WINDOWS
#define path dd_name
#define handle dd_handle
#endif
    if (dirp->handle != INVALID_HANDLE_VALUE) {
        if (!FindClose(dirp->handle)) {
            errno = EBADF;
            return -1;
        }
        dirp->handle = INVALID_HANDLE_VALUE;
    }
#ifndef MOE_WINDOWS
    free(dirp->path);
#endif
    free(dirp);
    return 0;
#ifdef MOE_WINDOWS
#undef path
#undef handle
#endif
}

void
rewinddir(DIR *dirp)
{
#ifdef MOE_WINDOWS
#define path dd_name
#define handle dd_handle
#define find_data dd_dta
#define FindFirstFile _findfirst
#endif
    if (dirp->handle != INVALID_HANDLE_VALUE) {
        FindClose(dirp->handle);
    }
    dirp->handle = FindFirstFile(dirp->path, &dirp->find_data);
#ifdef MOE_WINDOWS
#undef path
#undef handle
#undef find_data
#undef FindFirstFile
#endif
}
