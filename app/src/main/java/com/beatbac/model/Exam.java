package com.beatbac.model;

import java.io.Serializable;

public class Exam implements Serializable {

    private String name, section, file,
            urlE116, urlE216, urlE117, urlE217, urlE118, urlE218, urlE119, urlE219,
            urlS116, urlS216, urlS117, urlS217, urlS118, urlS218, urlS119, urlS219;
    private int icon;

    public Exam(String name, String section, String file, int icon,
                String urlE116, String urlE216, String urlE117, String urlE217, String urlE118, String urlE218, String urlE119, String urlE219,
                String urlS116, String urlS216, String urlS117, String urlS217, String urlS118, String urlS218, String urlS119, String urlS219) {
        this.name = name;
        this.section = section;
        this.file = file;
        this.icon = icon;

        this.urlE116 = urlE116;
        this.urlE216 = urlE216;
        this.urlE117 = urlE117;
        this.urlE217 = urlE217;
        this.urlE118 = urlE118;
        this.urlE218 = urlE218;
        this.urlE119 = urlE119;
        this.urlE219 = urlE219;

        this.urlS116 = urlS116;
        this.urlS216 = urlS216;
        this.urlS117 = urlS117;
        this.urlS217 = urlS217;
        this.urlS118 = urlS118;
        this.urlS218 = urlS218;
        this.urlS119 = urlS119;
        this.urlS219 = urlS219;
    }

    public String getName() {
        return name;
    }

    public String getSection() {
        return section;
    }

    public int getIcon() {
        return icon;
    }

    public String getUrl(int i) {
        if (i == 0)
            return urlE116;
        if (i == 1)
            return urlE216;
        if (i == 2)
            return urlE117;
        if (i == 3)
            return urlE217;
        if (i == 4)
            return urlE118;
        if (i == 5)
            return urlE218;
        if (i == 6)
            return urlE119;
        if (i == 7)
            return urlE219;
        if (i == 8)
            return urlS116;
        if (i == 9)
            return urlS216;
        if (i == 10)
            return urlS117;
        if (i == 11)
            return urlS217;
        if (i == 12)
            return urlS118;
        if (i == 13)
            return urlS218;
        if (i == 14)
            return urlS119;
        if (i == 15)
            return urlS219;

        return null;
    }

    public String getFile(int i) {
        if (i == 0)
            return this.file + "-E1-2016";
        if (i == 1)
            return this.file + "-E2-2016";
        if (i == 2)
            return this.file + "-E1-2017";
        if (i == 3)
            return this.file + "-E2-2017";
        if (i == 4)
            return this.file + "-E1-2018";
        if (i == 5)
            return this.file + "-E2-2018";
        if (i == 6)
            return this.file + "-E1-2019";
        if (i == 7)
            return this.file + "-E2-2019";
        if (i == 8)
            return this.file + "-S1-2016";
        if (i == 9)
            return this.file + "-S2-2016";
        if (i == 10)
            return this.file + "-S1-2017";
        if (i == 11)
            return this.file + "-S2-2017";
        if (i == 12)
            return this.file + "-S1-2018";
        if (i == 13)
            return this.file + "-S2-2018";
        if (i == 14)
            return this.file + "-S1-2019";
        if (i == 15)
            return this.file + "-S2-2019";

        return null;
    }

}
