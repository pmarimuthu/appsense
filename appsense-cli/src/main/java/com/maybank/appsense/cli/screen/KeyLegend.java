package com.maybank.appsense.cli.screen;

import com.maybank.appsense.cli.color.AnsiColorUtil;

/** Renders the single-line key-binding footer. */
public final class KeyLegend {

    private static final String LEGEND =
            AnsiColorUtil.DIM + "  Space=pause  /=search  a/e/w=level"
            + "  d=since-deploy  x=discard  m=mark  c=copy  q=quit"
            + AnsiColorUtil.RESET;

    /** Returns the legend line (already ANSI-coloured). */
    public String render() {
        return LEGEND;
    }
}
