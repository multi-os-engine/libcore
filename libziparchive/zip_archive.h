/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Read-only access to Zip archives, with minimal heap allocation.
 */
#ifndef LIBZIPARCHIVE_ZIPARCHIVE_H_
#define LIBZIPARCHIVE_ZIPARCHIVE_H_

#include <sys/types.h>

namespace ziparchive {

/*
 * One entry in the hash table.
 */
struct ZipEntryName {
    const char* name;
    unsigned short nameLen;
};

/*
 * Read-only Zip archive.
 *
 * We want "open" and "find entry by name" to be fast operations, and
 * we want to use as little memory as possible.  We memory-map the zip
 * central directory, and load a hash table with pointers to the filenames
 * (which aren't null-terminated).  The other fields are at a fixed offset
 * from the filename, so we don't need to extract those (but we do need
 * to byte-read and endian-swap them every time we want them).
 *
 * It's possible that somebody has handed us a massive (~1GB) zip archive,
 * so we can't expect to mmap the entire file.
 *
 * To speed comparisons when doing a lookup by name, we could make the mapping
 * "private" (copy-on-write) and null-terminate the filenames after verifying
 * the record structure.  However, this requires a private mapping of
 * every page that the Central Directory touches.  Easier to tuck a copy
 * of the string length into the hash table entry.
 */
struct ZipArchive {
    /* open Zip archive */
    int mFd;

    /* mapped central directory area */
    off_t mDirectoryOffset;
    void* mDirectoryMap;

    /* number of entries in the Zip archive */
    int mNumEntries;

    /*
     * We know how many entries are in the Zip archive, so we can have a
     * fixed-size hash table.  We probe on collisions.
     */
    int mHashTableSize;
    ZipHashEntry* mHashTable;
};

struct ZipEntry {
  const uint32_t method;
  const size_t uncompLength;
  const size_t compLen;
  const off_t offset;
  const uint64_t modWhen;
  const uint32_t crc32;
  const ZipEntryName* const name;

  ZipEntry(const ZipEntryName* name,
           uint32_t method, size_t uncompLength,
           size_t compLength, size_t offset, uint64_t modWhen,
           uint32_t crc32);
};

/* Zip compression methods we support */
enum {
    kCompressStored     = 0,        // no compression
    kCompressDeflated   = 8,        // standard deflate
};


/*
 * Open a Zip archive.
 *
 * On success, returns 0 and populates "pArchive".  Returns nonzero errno
 * value on failure.
 */
const ZipArchive* OpenArchive(const char* fileName);

/*
 * Like OpenArchive, but takes a file descriptor open for reading
 * at the start of the file.  The descriptor must be mappable (this does
 * not allow access to a stream).
 *
 * "debugFileName" will appear in error messages, but is not otherwise used.
 */
const ZipArchive* OpenArchive(int fd, const char* debugFileName);

/*
 * Close archive, releasing resources associated with it.
 *
 * Depending on the implementation this could unmap pages used by classes
 * stored in a Jar.  This should only be done after unloading classes.
 */
void CloseArchive(ZipArchive* pArchive);

/*
 * Find an entry in the Zip archive, by name.  Returns NULL if the entry
 * was not found.
 */
const ZipEntry* FindEntry(const ZipArchive* pArchive, const char* entryName);

/*
 * Uncompress and write an entry to a file descriptor.
 *
 * Returns 0 on success.
 */
int ExtractEntryToFile(const ZipArchive* pArchive,
    const ZipEntry* entry, int fd);
}  // namespace ziparhive

#endif  // LIBZIPARCHIVE_ZIPARCHIVE_H_
