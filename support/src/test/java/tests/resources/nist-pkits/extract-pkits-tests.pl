#!/usr/bin/env perl

use strict;

my $enabled = 0;
my $readingPath = 0;
my $sectionName;
my $testNumber;
my $testName;
my $pathEntry = "";
my $expectedOutcome;
my @pathEntries;

my $delimiter = "\x{2022}";
utf8::encode($delimiter);

sub trim($) {
    my $s = shift;
    $s =~ s/^\s+//g;
    $s =~ s/\s+$//g;
    return $s;
}

sub printTest() {
    my @certNames;
    my @crlNames;

    foreach my $entry (@pathEntries) {
        $entry =~ s/ //g;
        $entry =~ s/-//g;
        my @parts = split(/,/, $entry);
        push(@certNames, $parts[0]);
        if ($#parts > 0) {
            for (1..$#parts) {
                push(@crlNames, $parts[$_]);
            }
        }
    }

    print <<EOF;
    /** NIST PKITS test ${testNumber} */
    public void test${sectionName}_${testName}() throws Exception {
EOF
    print " " x 8 . "String trustAnchor = \"" . (shift @certNames) . ".crt\";\n";

    print <<EOF;

        String[] certs = {
EOF
    
    # Print the CertPath in reverse order.
    for (0..$#certNames) {
        print " " x 16 . "\"${certNames[$#certNames - $_]}.crt\",\n";
    }
    print <<EOF;
        };

        String[] crls = {
EOF
    foreach my $crlName (@crlNames) {
        print " " x 16 . "\"${crlName}.crl\",\n";
    }
    print <<EOF;
        };

EOF
    if ($expectedOutcome) {
        print <<EOF;
        assertValidPath(trustAnchor, certs, crls);
EOF
    } else {
        print <<EOF;
        assertInvalidPath(trustAnchor, certs, crls);
EOF
    }

        print <<EOF;
    }

EOF
}
    
sub stopReadingPath() {
    if ($readingPath) {
        if (defined($pathEntry) and $pathEntry ne "") {
            push(@pathEntries, $pathEntry);
            $pathEntry = "";
        }

        printTest();
        @pathEntries = ();
        $readingPath = 0;
    }
}

while (<STDIN>) {
    chomp;

    if ($_ =~ /^\s*4 Certification Path Validation Tests$/) {
        $enabled = 1;
        next;
    }

    if ($_ =~ /^\s*4\.8 Certificate Policies\s*$/) {
        stopReadingPath();
        $enabled = 0;

        print " "x4 . "// TODO: skipping sections 4.8 and 4.9\n\n";
        next;
    }

    if ($_ =~ /^\s*4\.9 Require Explicit Policy\s*$/) {
        $enabled = 1;
        next;
    }

    if ($_ =~ /^\s*4\.10 Policy Mappings\s*$/) {
        stopReadingPath();
        $enabled = 0;

        print " "x4 . "// TODO: skipping sections 4.10, 4.11, and 4.12\n\n";
        next;
    }

    if ($_ =~ /^\s*4\.13 Name Constraints\s*$/) {
        $enabled = 1;
        next;
    }

    if ($_ =~ /^\s*5 Relationship to Previous Test Suite\s*[^.]/) {
        stopReadingPath();
        $enabled = 0;
        exit;
    }

    if (!$enabled) {
        next;
    }

    if ($_ =~ /^\s*4\.[0-9]+ (.*)$/) {
        stopReadingPath();
        $sectionName = $1;
        $sectionName =~ s/ //g;
        $sectionName =~ s/-//g;
    }

    if ($_ =~ /^\s*(4\.[0-9]+\.[0-9]+) (.*)$/) {
        stopReadingPath();
        $testNumber = $1;
        $testName = $2;
        $testName =~ s/ //g;
        $testName =~ s/-//g;
    }

    if ($_ =~ /Expected Result:.*(should validate|should not validate)/) {
        if ($1 eq "should validate") {
            $expectedOutcome = 1;
        } else {
            $expectedOutcome = 0;
        }
    } elsif ($_ =~ /Expected Result:/) {
        die "Can not determine expected result for test:\n\t${testName}";
    }

    if ($_ =~ /^\s*Certification Path:/) {
        $readingPath = 1;
        next;
    }

    if ($readingPath) {
        # Page number from the PDF
        if (trim($_) =~ /^[0-9]+$/) {
            next;
        }

        if ($_ =~ /${delimiter}\s*(.*)$/u) {
            if (defined($pathEntry) and $pathEntry ne "") {
                push(@pathEntries, $pathEntry);
            }
            $pathEntry = trim($1);
        } else {
            if ($_ =~ /The certification path is composed of the following objects:(.*)$/) {
                $pathEntry = trim($1);
            } else {
                $pathEntry .= trim($_);
            }
        }
    }
}
