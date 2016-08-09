/*
 * Copyright (c) 1994, 2010, Oracle and/or its affiliates. All rights reserved.
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
 * Pathname canonicalization for Unix file systems
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <errno.h>
#include <limits.h>
#if !defined(_ALLBSD_SOURCE) && !defined(MOE_WINDOWS)
#include <alloca.h>
#endif


/* Note: The comments in this file use the terminology
         defined in the java.io.File class */


/* Check the given name sequence to see if it can be further collapsed.
   Return zero if not, otherwise return the number of names in the sequence. */

static int
collapsible(char *names)
{
    char *p = names;
    int dots = 0, n = 0;

    while (*p) {
        if ((p[0] == '.') && ((p[1] == '\0')
#ifndef MOE_WINDOWS
                              || (p[1] == '/')
#else
                              || (p[1] == '\\')
#endif
                              || ((p[1] == '.') && ((p[2] == '\0')
#ifndef MOE_WINDOWS
                                                    || (p[2] == '/'))))) {
#else
                                                    || (p[2] == '\\'))))) {
#endif
            dots = 1;
        }
        n++;
        while (*p) {
#ifndef MOE_WINDOWS
            if (*p == '/') {
#else
            if (*p == '\\') {
#endif
                p++;
                break;
            }
            p++;
        }
    }
    return (dots ? n : 0);
}


/* Split the names in the given name sequence,
   replacing slashes with nulls and filling in the given index array */

static void
splitNames(char *names, char **ix)
{
    char *p = names;
    int i = 0;

    while (*p) {
        ix[i++] = p++;
        while (*p) {
#ifndef MOE_WINDOWS
            if (*p == '/') {
#else
            if (*p == '\\') {
#endif
                *p++ = '\0';
                break;
            }
            p++;
        }
    }
}


/* Join the names in the given name sequence, ignoring names whose index
   entries have been cleared and replacing nulls with slashes as needed */

static void
joinNames(char *names, int nc, char **ix)
{
    int i;
    char *p;

    for (i = 0, p = names; i < nc; i++) {
        if (!ix[i]) continue;
        if (i > 0) {
#ifndef MOE_WINDOWS
            p[-1] = '/';
#else
            p[-1] = '\\';
#endif
        }
        if (p == ix[i]) {
            p += strlen(p) + 1;
        } else {
            char *q = ix[i];
            while ((*p++ = *q++));
        }
    }
    *p = '\0';
}


/* Collapse "." and ".." names in the given path wherever possible.
   A "." name may always be eliminated; a ".." name may be eliminated if it
   follows a name that is neither "." nor "..".  This is a syntactic operation
   that performs no filesystem queries, so it should only be used to cleanup
   after invoking the realpath() procedure. */

static void
collapse(char *path)
{
#ifndef MOE_WINDOWS
    char *names = (path[0] == '/') ? path + 1 : path; /* Preserve first '/' */
#else
    char *names = (isalpha(path[0]) && path[1] == ':') ? path + 3 : path; /* Preserve starting "[a-zA-Z]:\" */
#endif
    int nc;
    char **ix;
    int i, j;
    char *p, *q;

    nc = collapsible(names);
    if (nc < 2) return;         /* Nothing to do */
    ix = (char **)alloca(nc * sizeof(char *));
    splitNames(names, ix);

    for (i = 0; i < nc; i++) {
        int dots = 0;

        /* Find next occurrence of "." or ".." */
        do {
            char *p = ix[i];
            if (p[0] == '.') {
                if (p[1] == '\0') {
                    dots = 1;
                    break;
                }
                if ((p[1] == '.') && (p[2] == '\0')) {
                    dots = 2;
                    break;
                }
            }
            i++;
        } while (i < nc);
        if (i >= nc) break;

        /* At this point i is the index of either a "." or a "..", so take the
           appropriate action and then continue the outer loop */
        if (dots == 1) {
            /* Remove this instance of "." */
            ix[i] = 0;
        }
        else {
            /* If there is a preceding name, remove both that name and this
               instance of ".."; otherwise, leave the ".." as is */
            for (j = i - 1; j >= 0; j--) {
                if (ix[j]) break;
            }
            if (j < 0) continue;
            ix[j] = 0;
            ix[i] = 0;
        }
        /* i will be incremented at the top of the loop */
    }

    joinNames(names, nc, ix);
}

#ifdef MOE_WINDOWS
// Taken from jdk7/src/windows/native/java/io/canonicalize_md.c
int
lastErrorReportable()
{
    DWORD errval = GetLastError();
    if ((errval == ERROR_FILE_NOT_FOUND)
        || (errval == ERROR_DIRECTORY)
        || (errval == ERROR_PATH_NOT_FOUND)
        || (errval == ERROR_BAD_NETPATH)
        || (errval == ERROR_BAD_NET_NAME)
        || (errval == ERROR_ACCESS_DENIED)
        || (errval == ERROR_NETWORK_UNREACHABLE)
        || (errval == ERROR_NETWORK_ACCESS_DENIED)) {
        return 0;
    }

#ifdef DEBUG_PATH
    jio_fprintf(stderr, "canonicalize: errval %d\n", errval);
#endif
    return 1;
}

static char *
cp(char *dst, char *dend, char first, char *src, char *send)
{
    char *p = src, *q = dst;
    if (first != '\0') {
        if (q < dend) {
            *q++ = first;
        }
        else {
            errno = ENAMETOOLONG;
            return NULL;
        }
    }
    if (send - p > dend - q) {
        errno = ENAMETOOLONG;
        return NULL;
    }
    while (p < send) {
        *q++ = *p++;
    }
    return q;
}

int
canonicalizeWithPrefix(char* canonicalPrefix, char* pathWithCanonicalPrefix, char *result, int size)
{
    WIN32_FIND_DATA fd;
    HANDLE h;
    char *src, *dst, *dend;

    src = pathWithCanonicalPrefix;
    dst = result;        /* Place results here */
    dend = dst + size;   /* Don't go to or past here */

    h = FindFirstFile(pathWithCanonicalPrefix, &fd);    /* Look up file */
    if (h != INVALID_HANDLE_VALUE) {
        /* Lookup succeeded; concatenate true name to prefix */
        FindClose(h);
        if (!(dst = cp(dst, dend, '\0',
            canonicalPrefix,
            canonicalPrefix + strlen(canonicalPrefix)))) {
            return -1;
        }
        if (!(dst = cp(dst, dend, '\\',
            fd.cFileName,
            fd.cFileName + strlen(fd.cFileName)))) {
            return -1;
        }
    }
    else {
        if (!lastErrorReportable()) {
            if (!(dst = cp(dst, dend, '\0', src, src + strlen(src)))) {
                return -1;
            }
        }
        else {
            return -1;
        }
    }

    if (dst >= dend) {
        errno = ENAMETOOLONG;
        return -1;
    }
    *dst = '\0';
    return 0;
}
#endif

/* Convert a pathname to canonical form.  The input path is assumed to contain
   no duplicate slashes.  On Solaris we can use realpath() to do most of the
   work, though once that's done we still must collapse any remaining "." and
   ".." names by hand. */

int
canonicalize(char *original, char *resolved, int len)
{
    if (len < PATH_MAX) {
        errno = EINVAL;
        return -1;
    }

    if (strlen(original) > PATH_MAX) {
        errno = ENAMETOOLONG;
        return -1;
    }

    /* First try realpath() on the entire path */
    if (realpath(original, resolved)) {
        /* That worked, so return it */
        collapse(resolved);
        return 0;
    }
    else {
        /* Something's bogus in the original path, so remove names from the end
           until either some subpath works or we run out of names */
        char *p, *end, *r = NULL;
        char path[PATH_MAX + 1];

        strncpy(path, original, sizeof(path));
        if (path[PATH_MAX] != '\0') {
            errno = ENAMETOOLONG;
            return -1;
        }
        end = path + strlen(path);

        for (p = end; p > path;) {

            /* Skip last element */
#ifndef MOE_WINDOWS
            while ((--p > path) && (*p != '/'));
#else
            while ((--p > path) && (*p != '\\'));
#endif
            if (p == path) break;

            /* Try realpath() on this subpath */
            *p = '\0';
            r = realpath(path, resolved);
#ifndef MOE_WINDOWS
            *p = (p == end) ? '\0' : '/';
#else
            *p = (p == end) ? '\0' : '\\';
#endif

            if (r != NULL) {
                /* The subpath has a canonical path */
                break;
            }
            else if (errno == ENOENT || errno == ENOTDIR || errno == EACCES || errno == ENOTCONN) {
                /* If the lookup of a particular subpath fails because the file
                   does not exist, because it is of the wrong type, or because
                   access is denied, then remove its last name and try again.
                   Other I/O problems cause an error return. */

                /* NOTE: ENOTCONN seems like an odd errno to expect, but this is
                   the behaviour on linux for fuse filesystems when the fuse device
                   associated with the FS is closed but the filesystem is not
                   unmounted. */
                continue;
            }
            else {
                return -1;
            }
        }

        if (r != NULL) {
            /* Append unresolved subpath to resolved subpath */
            int rn = strlen(r);
            if (rn + (int)strlen(p) >= len) {
                /* Buffer overflow */
                errno = ENAMETOOLONG;
                return -1;
            }
#ifndef MOE_WINDOWS
            if ((rn > 0) && (r[rn - 1] == '/') && (*p == '/')) {
#else
            if ((rn > 0) && (r[rn - 1] == '\\') && (*p == '\\')) {
#endif
                /* Avoid duplicate slashes */
                p++;
            }
            strcpy(r + rn, p);
            collapse(r);
            return 0;
        }
        else {
            /* Nothing resolved, so just return the original path */
            strcpy(resolved, path);
            collapse(resolved);
            return 0;
        }
    }

}
