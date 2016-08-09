/*
 * Copyright (C) 2007 The Android Open Source Project
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

package libcore.util;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.TreeSet;

import libcore.io.BufferIterator;
import libcore.io.HeapBufferIterator;
import libcore.io.IoUtils;

final class TzDataCustom extends ZoneInfoDB.TzData {

    private static final String ZONE_DIRECTORY_NAME = "/usr/share/zoneinfo/";
    private static final String VERSION = readVersion();
    private static final Map<String, String> deprecatedAliases;
    private static ZoneInfo[] zoneInfos;
    private String zoneTab;
    private static String[] ids;

    static {
        deprecatedAliases = new HashMap<String, String>();
        deprecatedAliases.put("ACT", "Australia/Darwin");
        deprecatedAliases.put("AET", "Australia/Sydney");
        deprecatedAliases.put("AGT", "America/Argentina/Buenos_Aires");
        deprecatedAliases.put("ART", "Africa/Cairo");
        deprecatedAliases.put("AST", "America/Juneau");
        deprecatedAliases.put("BET", "Etc/GMT-11");
        deprecatedAliases.put("BST", "Asia/Colombo");
        deprecatedAliases.put("CAT", "Africa/Gaborone");
        deprecatedAliases.put("CNT", "America/St_Johns");
        deprecatedAliases.put("CST", "CST6CDT");
        deprecatedAliases.put("CTT", "Asia/Brunei");
        deprecatedAliases.put("EAT", "Indian/Comoro");
        deprecatedAliases.put("ECT", "CET");
        deprecatedAliases.put("EST", "EST5EDT");
        deprecatedAliases.put("EST5", "EST5EDT");
        deprecatedAliases.put("IET", "EST5EDT");
        deprecatedAliases.put("IST", "Asia/Calcutta");
        deprecatedAliases.put("JST", "Asia/Seoul");
        deprecatedAliases.put("MIT", "Pacific/Niue");
        deprecatedAliases.put("MST", "MST7MDT");
        deprecatedAliases.put("MST7", "MST7MDT");
        deprecatedAliases.put("NET", "Indian/Mauritius");
        deprecatedAliases.put("NST", "Pacific/Auckland");
        deprecatedAliases.put("PLT", "Indian/Kerguelen");
        deprecatedAliases.put("PNT", "MST7MDT");
        deprecatedAliases.put("PRT", "America/Anguilla");
        deprecatedAliases.put("PST", "PST8PDT");
        deprecatedAliases.put("SST", "Pacific/Ponape");
        deprecatedAliases.put("VST", "Asia/Bangkok");        
        
        readIndex();
    }

    private static String readVersion() {
        try {
            byte[] bytes = IoUtils.readFileAsByteArray(ZONE_DIRECTORY_NAME + "+VERSION");
            return new String(bytes, 0, bytes.length, StandardCharsets.ISO_8859_1).trim();
        } catch (IOException ex) {
            return "unknown";
        }            
    }

    private static void readIndex() {
        try {
            readIndexMulti();
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
        
        zoneInfos = new ZoneInfo[ids.length];
    }

    private static void readIndexMulti() throws IOException {
        TreeSet<String> set = new TreeSet<String>();
        
        File zoneDir = new File(ZONE_DIRECTORY_NAME);
        File[] zoneDirFiles = zoneDir.listFiles();
        if(zoneDirFiles != null && zoneDirFiles.length != 0){
            for (File f1 : zoneDirFiles) {
                String name1 = f1.getName();
                if ("Factory".equals(name1)) {
                    continue;
                }
                if (name1.charAt(0) < 'A' || name1.charAt(0) > 'Z') {
                    continue;
                }
                if (f1.isDirectory()) {
                    File[] f1Files = f1.listFiles();
                    if(f1Files != null && f1Files.length != 0){
                        for (File f2 : f1Files) {
                            String name2 = f2.getName();
                            if (name2.charAt(0) < 'A' || name2.charAt(0) > 'Z') {
                                continue;
                            }
                            if (f2.isDirectory()) {
                                File[] f2Files = f2.listFiles();
                                if(f2Files != null && f2Files.length != 0){
                                    for (File f3 : f2Files) {
                                        String name3 = f3.getName();
                                        if (name3.charAt(0) < 'A' || name3.charAt(0) > 'Z') {
                                            continue;
                                        }
                                        if (!f3.isDirectory()) {
                                            set.add(name1 + "/" + name2 + "/" + name3);
                                        }
                                    }
                                }
                            } else {
                                set.add(name1 + "/" + name2);                                
                            }
                        }
                    }
                } else {
                    set.add(name1);
                }
            }
        }
        
        for (Entry<String, String> alias : deprecatedAliases.entrySet()) {
            if (set.contains(alias.getValue())) {
                set.add(alias.getKey());
            }
        }

        ids = set.toArray(new String[set.size()]);
    }
    
    public TimeZone makeTimeZone(String id) throws IOException {
        return makeTimeZone(id, true);
    }
    
    private TimeZone makeTimeZone(String id, boolean clone) throws IOException {
        String realId = deprecatedAliases.get(id);
        if (realId != null) {
            return makeTimeZone(realId, clone);
        }
      
        int index = Arrays.binarySearch(ids, id);
        if (index < 0) {
            return null;
        }

        ZoneInfo zoneInfo = zoneInfos[index];
        if (zoneInfo != null) {
            return clone ? (TimeZone) zoneInfo.clone() : zoneInfo;
        }
        
        byte[] bytes = IoUtils.readFileAsByteArray(ZONE_DIRECTORY_NAME + id);
        BufferIterator it = HeapBufferIterator.iterator(bytes, 0, bytes.length, ByteOrder.BIG_ENDIAN);

        zoneInfo = (ZoneInfo) ZoneInfo.makeTimeZone(id, it);
        zoneInfos[index] = zoneInfo;
        if(zoneInfo != null){
           return clone ? (TimeZone) zoneInfo.clone() : zoneInfo; 
       } else {
           return zoneInfo;
       }
    }

    public String[] getAvailableIDs() {
        return ids.clone();
    }

    public String[] getAvailableIDs(int rawOffset) {
        List<String> matches = new ArrayList<String>();
        for (String id : ids) {
            try {
                TimeZone timeZone = makeTimeZone(id, false);
                if (timeZone != null && timeZone.getRawOffset() == rawOffset) {
                    matches.add(id);
                }
            } catch (IOException e) {
            }
        }
        return matches.toArray(new String[matches.size()]);
    }

    public String getZoneTab() {
        if (zoneTab == null) {
            try {
                zoneTab = IoUtils.readFileAsString(ZONE_DIRECTORY_NAME + "zone.tab");
            } catch (IOException e) {
                throw new Error(e);
            }
        }
        return zoneTab;
    }

    @Override
    public String getDefaultID() {
        String zoneName = null;
        try {
            zoneName = IoUtils.readFileAsString("/etc/timezone").trim();
        } catch (IOException e) {
        }
        if (zoneName == null || zoneName.isEmpty()) {
            File link = new File("/etc/localtime");
            if (!link.exists()) {
                link = new File("/private/var/db/timezone/localtime");
            }
            try {
                String path = link.getCanonicalPath(); 
                for (String id : ids) {
                    if (path.endsWith(id)) {
                        zoneName = id;
                    }
                }
            } catch (IOException e) {
            }
        }
        return zoneName;
    }
    
    public String getVersion() {
        return VERSION;
    }
}
