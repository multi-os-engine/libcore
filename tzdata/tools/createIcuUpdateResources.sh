#!/bin/bash 

TZ_DATA_FILE=$1

START_DIR=`pwd`
ICU_DIR=../../../../external/icu/icu4c/source/
BUILD_DIR=${START_DIR}/icu_build

rm -rf ${BUILD_DIR}
mkdir -p ${BUILD_DIR}
cd ${BUILD_DIR}

set -e

${ICU_DIR}/runConfigureICU Linux
mkdir -p ${BUILD_DIR}/bin
cd ${BUILD_DIR}/tools/tzcode
ln -s ${ICU_DIR}/tools/tzcode/icuregions ./icuregions
ln -s ${ICU_DIR}/tools/tzcode/icuzones ./icuzones
cp ${TZ_DATA_FILE} .
make

# Then make the whole thing?
cd ${BUILD_DIR}
make -j32

ICU_LIB_DIR=${BUILD_DIR}/lib
BIN_DIR=${BUILD_DIR}/bin
TZ_FILES=tzdata.lst

echo metaZones.res > ${TZ_FILES}
echo timezoneTypes.res >> ${TZ_FILES}
echo windowsZones.res >> ${TZ_FILES}
echo zoneinfo64.res >> ${TZ_FILES}

# Copy all the .res files we need here a from ./data/out/build/icudt54l
RES_DIR=data/out/build/icudt54l
cp ${RES_DIR}/metaZones.res ${BUILD_DIR}
cp ${RES_DIR}/timezoneTypes.res ${BUILD_DIR}
cp ${RES_DIR}/windowsZones.res ${BUILD_DIR}
cp ${RES_DIR}/zoneinfo64.res ${BUILD_DIR}

LD_LIBRARY_PATH=${ICU_LIB_DIR} ${BIN_DIR}/pkgdata -F -m common -v -T . -d . -p icu_tzdata ${TZ_FILES}
cp icu_tzdata.dat ${START_DIR}
