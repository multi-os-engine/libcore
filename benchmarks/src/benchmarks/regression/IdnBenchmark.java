package benchmarks.regression;


import com.google.caliper.SimpleBenchmark;

import java.net.IDN;

public class IdnBenchmark extends SimpleBenchmark {

    public void timeToUnicode(int reps) {
        for (int i = 0; i < reps; i++) {
        IDN.toASCII("fass.de");
        IDN.toASCII("faß.de");
        IDN.toASCII("fäß.de");
        IDN.toASCII("₹.com");
        IDN.toASCII("\uD804\uDC13.com");
        IDN.toASCII("a\u200Cb");
        IDN.toASCII("öbb.at");
        IDN.toASCII("ȡog.de");
        IDN.toASCII("☕.de");
        IDN.toASCII("i♥ny.de");
        IDN.toASCII("abc・日本.co.jp");
        IDN.toASCII("日本.co.jp");
        IDN.toASCII("x\u0327\u0301.de");
        IDN.toASCII("σόλοσ.gr");
        IDN.toASCII("σόλοσ.grعربي.de");
        }
    }

    public void timeToAscii(int reps) {
        for (int i = 0; i < reps; i++) {
        IDN.toUnicode("xn--fss-qla.de");
        IDN.toUnicode("xn--yzg.com");
        IDN.toUnicode("xn--n00d.com");
        IDN.toUnicode("xn--bb-eka.at");
        IDN.toUnicode("xn--og-09a.de");
        IDN.toUnicode("xn--53h.de");
        IDN.toUnicode("xn--iny-zx5a.de");
        IDN.toUnicode("xn--abc-rs4b422ycvb.co.jp");
        IDN.toUnicode("xn--wgv71a.co.jp");
        IDN.toUnicode("xn--x-xbb7i.de");
        IDN.toUnicode("xn--wxaikc6b.gr");
        IDN.toUnicode("xn--wxaikc6b.xn--gr-gtd9a1b0g.de");
        }
    }

}
