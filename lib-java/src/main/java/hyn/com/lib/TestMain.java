package hyn.com.lib;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hanyanan on 2015/5/16.
 */
public class TestMain {
    private static Map<String, String> sMimeTypeMap = new HashMap<String, String>();
    static{
        sMimeTypeMap.put("application/andrew-inset", "ez");
        sMimeTypeMap.put("application/dsptype", "tsp");
        sMimeTypeMap.put("application/futuresplash", "spl");
        sMimeTypeMap.put("application/hta", "hta");
        sMimeTypeMap.put("application/mac-binhex40", "hqx");
        sMimeTypeMap.put("application/mac-compactpro", "cpt");
        sMimeTypeMap.put("application/mathematica", "nb");
        sMimeTypeMap.put("application/msaccess", "mdb");
        sMimeTypeMap.put("application/oda", "oda");
        sMimeTypeMap.put("application/ogg", "ogg");
        sMimeTypeMap.put("application/pdf", "pdf");
        sMimeTypeMap.put("application/pgp-keys", "key");
        sMimeTypeMap.put("application/pgp-signature", "pgp");
        sMimeTypeMap.put("application/pics-rules", "prf");
        sMimeTypeMap.put("application/rar", "rar");
        sMimeTypeMap.put("application/rdf+xml", "rdf");
        sMimeTypeMap.put("application/rss+xml", "rss");
        sMimeTypeMap.put("application/zip", "zip");
        sMimeTypeMap.put("application/vnd.android.package-archive",
                "apk");
        sMimeTypeMap.put("application/vnd.cinderella", "cdy");
        sMimeTypeMap.put("application/vnd.ms-pki.stl", "stl");
        sMimeTypeMap.put(
                "application/vnd.oasis.opendocument.database", "odb");
        sMimeTypeMap.put(
                "application/vnd.oasis.opendocument.formula", "odf");
        sMimeTypeMap.put(
                "application/vnd.oasis.opendocument.graphics", "odg");
        sMimeTypeMap.put(
                "application/vnd.oasis.opendocument.graphics-template",
                "otg");
        sMimeTypeMap.put(
                "application/vnd.oasis.opendocument.image", "odi");
        sMimeTypeMap.put(
                "application/vnd.oasis.opendocument.spreadsheet", "ods");
        sMimeTypeMap.put(
                "application/vnd.oasis.opendocument.spreadsheet-template",
                "ots");
        sMimeTypeMap.put(
                "application/vnd.oasis.opendocument.text", "odt");
        sMimeTypeMap.put(
                "application/vnd.oasis.opendocument.text-master", "odm");
        sMimeTypeMap.put(
                "application/vnd.oasis.opendocument.text-template", "ott");
        sMimeTypeMap.put(
                "application/vnd.oasis.opendocument.text-web", "oth");
        sMimeTypeMap.put("application/msword", "doc");
        sMimeTypeMap.put("application/msword", "dot");
        sMimeTypeMap.put(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "docx");
        sMimeTypeMap.put(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
                "dotx");
        sMimeTypeMap.put("application/vnd.ms-excel", "xls");
        sMimeTypeMap.put("application/vnd.ms-excel", "xlt");
        sMimeTypeMap.put(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "xlsx");
        sMimeTypeMap.put(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
                "xltx");
        sMimeTypeMap.put("application/vnd.ms-powerpoint", "ppt");
        sMimeTypeMap.put("application/vnd.ms-powerpoint", "pot");
        sMimeTypeMap.put("application/vnd.ms-powerpoint", "pps");
        sMimeTypeMap.put(
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "pptx");
        sMimeTypeMap.put(
                "application/vnd.openxmlformats-officedocument.presentationml.template",
                "potx");
        sMimeTypeMap.put(
                "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
                "ppsx");
        sMimeTypeMap.put("application/vnd.rim.cod", "cod");
        sMimeTypeMap.put("application/vnd.smaf", "mmf");
        sMimeTypeMap.put("application/vnd.stardivision.calc", "sdc");
        sMimeTypeMap.put("application/vnd.stardivision.draw", "sda");
        sMimeTypeMap.put(
                "application/vnd.stardivision.impress", "sdd");
        sMimeTypeMap.put(
                "application/vnd.stardivision.impress", "sdp");
        sMimeTypeMap.put("application/vnd.stardivision.math", "smf");
        sMimeTypeMap.put("application/vnd.stardivision.writer",
                "sdw");
        sMimeTypeMap.put("application/vnd.stardivision.writer",
                "vor");
        sMimeTypeMap.put(
                "application/vnd.stardivision.writer-global", "sgl");
        sMimeTypeMap.put("application/vnd.sun.xml.calc", "sxc");
        sMimeTypeMap.put(
                "application/vnd.sun.xml.calc.template", "stc");
        sMimeTypeMap.put("application/vnd.sun.xml.draw", "sxd");
        sMimeTypeMap.put(
                "application/vnd.sun.xml.draw.template", "std");
        sMimeTypeMap.put("application/vnd.sun.xml.impress", "sxi");
        sMimeTypeMap.put(
                "application/vnd.sun.xml.impress.template", "sti");
        sMimeTypeMap.put("application/vnd.sun.xml.math", "sxm");
        sMimeTypeMap.put("application/vnd.sun.xml.writer", "sxw");
        sMimeTypeMap.put(
                "application/vnd.sun.xml.writer.global", "sxg");
        sMimeTypeMap.put(
                "application/vnd.sun.xml.writer.template", "stw");
        sMimeTypeMap.put("application/vnd.visio", "vsd");
        sMimeTypeMap.put("application/x-abiword", "abw");
        sMimeTypeMap.put("application/x-apple-diskimage", "dmg");
        sMimeTypeMap.put("application/x-bcpio", "bcpio");
        sMimeTypeMap.put("application/x-bittorrent", "torrent");
        sMimeTypeMap.put("application/x-cdf", "cdf");
        sMimeTypeMap.put("application/x-cdlink", "vcd");
        sMimeTypeMap.put("application/x-chess-pgn", "pgn");
        sMimeTypeMap.put("application/x-cpio", "cpio");
        sMimeTypeMap.put("application/x-debian-package", "deb");
        sMimeTypeMap.put("application/x-debian-package", "udeb");
        sMimeTypeMap.put("application/x-director", "dcr");
        sMimeTypeMap.put("application/x-director", "dir");
        sMimeTypeMap.put("application/x-director", "dxr");
        sMimeTypeMap.put("application/x-dms", "dms");
        sMimeTypeMap.put("application/x-doom", "wad");
        sMimeTypeMap.put("application/x-dvi", "dvi");
        sMimeTypeMap.put("application/x-flac", "flac");
        sMimeTypeMap.put("application/x-font", "pfa");
        sMimeTypeMap.put("application/x-font", "pfb");
        sMimeTypeMap.put("application/x-font", "gsf");
        sMimeTypeMap.put("application/x-font", "pcf");
        sMimeTypeMap.put("application/x-font", "pcf.Z");
        sMimeTypeMap.put("application/x-freemind", "mm");
        sMimeTypeMap.put("application/x-futuresplash", "spl");
        sMimeTypeMap.put("application/x-gnumeric", "gnumeric");
        sMimeTypeMap.put("application/x-go-sgf", "sgf");
        sMimeTypeMap.put("application/x-graphing-calculator", "gcf");
        sMimeTypeMap.put("application/x-gtar", "gtar");
        sMimeTypeMap.put("application/x-gtar", "tgz");
        sMimeTypeMap.put("application/x-gtar", "taz");
        sMimeTypeMap.put("application/x-hdf", "hdf");
        sMimeTypeMap.put("application/x-ica", "ica");
        sMimeTypeMap.put("application/x-internet-signup", "ins");
        sMimeTypeMap.put("application/x-internet-signup", "isp");
        sMimeTypeMap.put("application/x-iphone", "iii");
        sMimeTypeMap.put("application/x-iso9660-image", "iso");
        sMimeTypeMap.put("application/x-jmol", "jmz");
        sMimeTypeMap.put("application/x-kchart", "chrt");
        sMimeTypeMap.put("application/x-killustrator", "kil");
        sMimeTypeMap.put("application/x-koan", "skp");
        sMimeTypeMap.put("application/x-koan", "skd");
        sMimeTypeMap.put("application/x-koan", "skt");
        sMimeTypeMap.put("application/x-koan", "skm");
        sMimeTypeMap.put("application/x-kpresenter", "kpr");
        sMimeTypeMap.put("application/x-kpresenter", "kpt");
        sMimeTypeMap.put("application/x-kspread", "ksp");
        sMimeTypeMap.put("application/x-kword", "kwd");
        sMimeTypeMap.put("application/x-kword", "kwt");
        sMimeTypeMap.put("application/x-latex", "latex");
        sMimeTypeMap.put("application/x-lha", "lha");
        sMimeTypeMap.put("application/x-lzh", "lzh");
        sMimeTypeMap.put("application/x-lzx", "lzx");
        sMimeTypeMap.put("application/x-maker", "frm");
        sMimeTypeMap.put("application/x-maker", "maker");
        sMimeTypeMap.put("application/x-maker", "frame");
        sMimeTypeMap.put("application/x-maker", "fb");
        sMimeTypeMap.put("application/x-maker", "book");
        sMimeTypeMap.put("application/x-maker", "fbdoc");
        sMimeTypeMap.put("application/x-mif", "mif");
        sMimeTypeMap.put("application/x-ms-wmd", "wmd");
        sMimeTypeMap.put("application/x-ms-wmz", "wmz");
        sMimeTypeMap.put("application/x-msi", "msi");
        sMimeTypeMap.put("application/x-ns-proxy-autoconfig", "pac");
        sMimeTypeMap.put("application/x-nwc", "nwc");
        sMimeTypeMap.put("application/x-object", "o");
        sMimeTypeMap.put("application/x-oz-application", "oza");
        sMimeTypeMap.put("application/x-pkcs12", "p12");
        sMimeTypeMap.put("application/x-pkcs7-certreqresp", "p7r");
        sMimeTypeMap.put("application/x-pkcs7-crl", "crl");
        sMimeTypeMap.put("application/x-quicktimeplayer", "qtl");
        sMimeTypeMap.put("application/x-shar", "shar");
        sMimeTypeMap.put("application/x-shockwave-flash", "swf");
        sMimeTypeMap.put("application/x-stuffit", "sit");
        sMimeTypeMap.put("application/x-sv4cpio", "sv4cpio");
        sMimeTypeMap.put("application/x-sv4crc", "sv4crc");
        sMimeTypeMap.put("application/x-tar", "tar");
        sMimeTypeMap.put("application/x-texinfo", "texinfo");
        sMimeTypeMap.put("application/x-texinfo", "texi");
        sMimeTypeMap.put("application/x-troff", "t");
        sMimeTypeMap.put("application/x-troff", "roff");
        sMimeTypeMap.put("application/x-troff-man", "man");
        sMimeTypeMap.put("application/x-ustar", "ustar");
        sMimeTypeMap.put("application/x-wais-source", "src");
        sMimeTypeMap.put("application/x-wingz", "wz");
        sMimeTypeMap.put("application/x-webarchive", "webarchive");
        sMimeTypeMap.put("application/x-x509-ca-cert", "crt");
        sMimeTypeMap.put("application/x-x509-user-cert", "crt");
        sMimeTypeMap.put("application/x-xcf", "xcf");
        sMimeTypeMap.put("application/x-xfig", "fig");
        sMimeTypeMap.put("application/xhtml+xml", "xhtml");
        sMimeTypeMap.put("audio/3gpp", "3gpp");
        sMimeTypeMap.put("audio/amr", "amr");
        sMimeTypeMap.put("audio/basic", "snd");
        sMimeTypeMap.put("audio/midi", "mid");
        sMimeTypeMap.put("audio/midi", "midi");
        sMimeTypeMap.put("audio/midi", "kar");
        sMimeTypeMap.put("audio/midi", "xmf");
        sMimeTypeMap.put("audio/mobile-xmf", "mxmf");
        sMimeTypeMap.put("audio/mpeg", "mpga");
        sMimeTypeMap.put("audio/mpeg", "mpega");
        sMimeTypeMap.put("audio/mpeg", "mp2");
        sMimeTypeMap.put("audio/mpeg", "mp3");
        sMimeTypeMap.put("audio/mpeg", "m4a");
        sMimeTypeMap.put("audio/mpegurl", "m3u");
        sMimeTypeMap.put("audio/prs.sid", "sid");
        sMimeTypeMap.put("audio/x-aiff", "aif");
        sMimeTypeMap.put("audio/x-aiff", "aiff");
        sMimeTypeMap.put("audio/x-aiff", "aifc");
        sMimeTypeMap.put("audio/x-gsm", "gsm");
        sMimeTypeMap.put("audio/x-mpegurl", "m3u");
        sMimeTypeMap.put("audio/x-ms-wma", "wma");
        sMimeTypeMap.put("audio/x-ms-wax", "wax");
        sMimeTypeMap.put("audio/x-pn-realaudio", "ra");
        sMimeTypeMap.put("audio/x-pn-realaudio", "rm");
        sMimeTypeMap.put("audio/x-pn-realaudio", "ram");
        sMimeTypeMap.put("audio/x-realaudio", "ra");
        sMimeTypeMap.put("audio/x-scpls", "pls");
        sMimeTypeMap.put("audio/x-sd2", "sd2");
        sMimeTypeMap.put("audio/x-wav", "wav");
        sMimeTypeMap.put("image/bmp", "bmp");
        sMimeTypeMap.put("image/gif", "gif");
        sMimeTypeMap.put("image/ico", "cur");
        sMimeTypeMap.put("image/ico", "ico");
        sMimeTypeMap.put("image/ief", "ief");
        sMimeTypeMap.put("image/jpeg", "jpeg");
        sMimeTypeMap.put("image/jpeg", "jpg");
        sMimeTypeMap.put("image/jpeg", "jpe");
        sMimeTypeMap.put("image/pcx", "pcx");
        sMimeTypeMap.put("image/png", "png");
        sMimeTypeMap.put("image/svg+xml", "svg");
        sMimeTypeMap.put("image/svg+xml", "svgz");
        sMimeTypeMap.put("image/tiff", "tiff");
        sMimeTypeMap.put("image/tiff", "tif");
        sMimeTypeMap.put("image/vnd.djvu", "djvu");
        sMimeTypeMap.put("image/vnd.djvu", "djv");
        sMimeTypeMap.put("image/vnd.wap.wbmp", "wbmp");
        sMimeTypeMap.put("image/x-cmu-raster", "ras");
        sMimeTypeMap.put("image/x-coreldraw", "cdr");
        sMimeTypeMap.put("image/x-coreldrawpattern", "pat");
        sMimeTypeMap.put("image/x-coreldrawtemplate", "cdt");
        sMimeTypeMap.put("image/x-corelphotopaint", "cpt");
        sMimeTypeMap.put("image/x-icon", "ico");
        sMimeTypeMap.put("image/x-jg", "art");
        sMimeTypeMap.put("image/x-jng", "jng");
        sMimeTypeMap.put("image/x-ms-bmp", "bmp");
        sMimeTypeMap.put("image/x-photoshop", "psd");
        sMimeTypeMap.put("image/x-portable-anymap", "pnm");
        sMimeTypeMap.put("image/x-portable-bitmap", "pbm");
        sMimeTypeMap.put("image/x-portable-graymap", "pgm");
        sMimeTypeMap.put("image/x-portable-pixmap", "ppm");
        sMimeTypeMap.put("image/x-rgb", "rgb");
        sMimeTypeMap.put("image/x-xbitmap", "xbm");
        sMimeTypeMap.put("image/x-xpixmap", "xpm");
        sMimeTypeMap.put("image/x-xwindowdump", "xwd");
        sMimeTypeMap.put("model/iges", "igs");
        sMimeTypeMap.put("model/iges", "iges");
        sMimeTypeMap.put("model/mesh", "msh");
        sMimeTypeMap.put("model/mesh", "mesh");
        sMimeTypeMap.put("model/mesh", "silo");
        sMimeTypeMap.put("text/calendar", "ics");
        sMimeTypeMap.put("text/calendar", "icz");
        sMimeTypeMap.put("text/comma-separated-values", "csv");
        sMimeTypeMap.put("text/css", "css");
        sMimeTypeMap.put("text/html", "htm");
        sMimeTypeMap.put("text/html", "html");
        sMimeTypeMap.put("text/h323", "323");
        sMimeTypeMap.put("text/iuls", "uls");
        sMimeTypeMap.put("text/mathml", "mml");
        // add it first so it will be the default for ExtensionFromMimeType
        sMimeTypeMap.put("text/plain", "txt");
        sMimeTypeMap.put("text/plain", "asc");
        sMimeTypeMap.put("text/plain", "text");
        sMimeTypeMap.put("text/plain", "diff");
        sMimeTypeMap.put("text/plain", "po");     // reserve "pot" for vnd.ms-powerpoint
        sMimeTypeMap.put("text/richtext", "rtx");
        sMimeTypeMap.put("text/rtf", "rtf");
        sMimeTypeMap.put("text/texmacs", "ts");
        sMimeTypeMap.put("text/text", "phps");
        sMimeTypeMap.put("text/tab-separated-values", "tsv");
        sMimeTypeMap.put("text/xml", "xml");
        sMimeTypeMap.put("text/x-bibtex", "bib");
        sMimeTypeMap.put("text/x-boo", "boo");
        sMimeTypeMap.put("text/x-c++hdr", "h++");
        sMimeTypeMap.put("text/x-c++hdr", "hpp");
        sMimeTypeMap.put("text/x-c++hdr", "hxx");
        sMimeTypeMap.put("text/x-c++hdr", "hh");
        sMimeTypeMap.put("text/x-c++src", "c++");
        sMimeTypeMap.put("text/x-c++src", "cpp");
        sMimeTypeMap.put("text/x-c++src", "cxx");
        sMimeTypeMap.put("text/x-chdr", "h");
        sMimeTypeMap.put("text/x-component", "htc");
        sMimeTypeMap.put("text/x-csh", "csh");
        sMimeTypeMap.put("text/x-csrc", "c");
        sMimeTypeMap.put("text/x-dsrc", "d");
        sMimeTypeMap.put("text/x-haskell", "hs");
        sMimeTypeMap.put("text/x-java", "java");
        sMimeTypeMap.put("text/x-literate-haskell", "lhs");
        sMimeTypeMap.put("text/x-moc", "moc");
        sMimeTypeMap.put("text/x-pascal", "p");
        sMimeTypeMap.put("text/x-pascal", "pas");
        sMimeTypeMap.put("text/x-pcs-gcd", "gcd");
        sMimeTypeMap.put("text/x-setext", "etx");
        sMimeTypeMap.put("text/x-tcl", "tcl");
        sMimeTypeMap.put("text/x-tex", "tex");
        sMimeTypeMap.put("text/x-tex", "ltx");
        sMimeTypeMap.put("text/x-tex", "sty");
        sMimeTypeMap.put("text/x-tex", "cls");
        sMimeTypeMap.put("text/x-vcalendar", "vcs");
        sMimeTypeMap.put("text/x-vcard", "vcf");
        sMimeTypeMap.put("video/3gpp", "3gpp");
        sMimeTypeMap.put("video/3gpp", "3gp");
        sMimeTypeMap.put("video/3gpp", "3g2");
        sMimeTypeMap.put("video/dl", "dl");
        sMimeTypeMap.put("video/dv", "dif");
        sMimeTypeMap.put("video/dv", "dv");
        sMimeTypeMap.put("video/fli", "fli");
        sMimeTypeMap.put("video/m4v", "m4v");
        sMimeTypeMap.put("video/mpeg", "mpeg");
        sMimeTypeMap.put("video/mpeg", "mpg");
        sMimeTypeMap.put("video/mpeg", "mpe");
        sMimeTypeMap.put("video/mp4", "mp4");
        sMimeTypeMap.put("video/mpeg", "VOB");
        sMimeTypeMap.put("video/quicktime", "qt");
        sMimeTypeMap.put("video/quicktime", "mov");
        sMimeTypeMap.put("video/vnd.mpegurl", "mxu");
        sMimeTypeMap.put("video/x-la-asf", "lsf");
        sMimeTypeMap.put("video/x-la-asf", "lsx");
        sMimeTypeMap.put("video/x-mng", "mng");
        sMimeTypeMap.put("video/x-ms-asf", "asf");
        sMimeTypeMap.put("video/x-ms-asf", "asx");
        sMimeTypeMap.put("video/x-ms-wm", "wm");
        sMimeTypeMap.put("video/x-ms-wmv", "wmv");
        sMimeTypeMap.put("video/x-ms-wmx", "wmx");
        sMimeTypeMap.put("video/x-ms-wvx", "wvx");
        sMimeTypeMap.put("video/x-msvideo", "avi");
        sMimeTypeMap.put("video/x-sgi-movie", "movie");
        sMimeTypeMap.put("x-conference/x-cooltalk", "ice");
        sMimeTypeMap.put("x-epoc/x-sisx-app", "sisx");

        // Some more mime-pairs
        sMimeTypeMap.put("video/vnd.rn-realmedia", "rmvb");
        sMimeTypeMap.put("video/vnd.rn-realmedia", "rm");
        sMimeTypeMap.put("video/vnd.rn-realvideo", "rv");
        sMimeTypeMap.put("video/x-flv", "flv");
        sMimeTypeMap.put("video/x-flv", "hlv");
        sMimeTypeMap.put("video/x-matroska", "mkv");
        sMimeTypeMap.put("audio/vnd.rn-realaudio", "ra");
        sMimeTypeMap.put("audio/vnd.rn-realaudio", "ram");
        sMimeTypeMap.put("text/plain", "lrc");

        sMimeTypeMap.put("application/json", "json");
    }
    public static void main(String[] argv) {

    }
}
