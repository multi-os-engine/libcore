#!/usr/bin/env bash

usage() {
    echo "$0: generates test cases from the NIST PKITS documentation"
    echo ""
    echo "Usage: $0 PKITS.pdf <top of android tree>"
    exit 1
}

if [ $# -ne 2 ]; then
	usage
elif [ ! -f "${1}" ]; then
    echo "The first argument must point to PKITS.pdf"
    usage
fi

PDF="${1}"

TOP="${2}"
TARGET="${TOP}/libcore/luni/src/test/java/libcore/java/security/cert/X509CertificateNistPkitsTest.java"

if [ ! -f "${TARGET}" ]; then
    echo "Can not file file:"
    echo "    ${TARGET}"
    echo "The second argument must point to a valid Android tree"
    usage
fi

PDFTOTEXT=$(which pdftotext)
if [ -z "${PDFTOTEXT}" -o ! -x "${PDFTOTEXT}" ]; then
    echo "pdftotext must be installed. Try"
    echo "    apt-get install pdftotext"
    exit 1
fi

TEMP_TEXT=$(mktemp --tmpdir PKITS.txt.XXXXXXXX)
TEMP_JAVA=$(mktemp --tmpdir generated-nist-tests.XXXXXXXXX)

${PDFTOTEXT} -layout -nopgbrk -eol unix "${PDF}" "${TEMP_TEXT}"

"$(dirname $0)/extract-pkits-tests.pl" < "${TEMP_TEXT}" > "${TEMP_JAVA}"
sed -i '/DO NOT MANUALLY EDIT -- BEGIN AUTOMATICALLY GENERATED TESTS/,/DO NOT MANUALLY EDIT -- END AUTOMATICALLY GENERATED TESTS/{//!d}' "${TARGET}"
sed -i '/DO NOT MANUALLY EDIT -- BEGIN AUTOMATICALLY GENERATED TESTS/r '"${TEMP_JAVA}" "${TARGET}"
