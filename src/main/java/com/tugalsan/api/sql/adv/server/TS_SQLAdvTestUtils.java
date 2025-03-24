package com.tugalsan.api.sql.adv.server;

import com.tugalsan.api.function.client.maythrowexceptions.checked.TGS_FuncMTCUtils;
import java.util.*;
import com.tugalsan.api.list.client.*;
import com.tugalsan.api.string.client.*;
import com.tugalsan.api.log.server.*;
import com.tugalsan.api.sql.col.typed.client.*;
import com.tugalsan.api.sql.conn.server.*;
import com.tugalsan.api.sql.select.server.*;


public class TS_SQLAdvTestUtils {

    final private static TS_Log d = TS_Log.of(TS_SQLAdvTestUtils.class);

    public static void testVariables(TS_SQLConnAnchor anchor) {
        var arrName1 = "@ia";
        var arrName2 = "@ib";
        List<String> ia = TGS_ListUtils.of();
        List<Long> ib = TGS_ListUtils.of();
        ib.add(11L);
        ib.add(12L);
        var varName1 = "@x";
        var varName2 = "@y";
        ia.add(varName1);
        ia.add(varName2);

        List<String> o = TGS_ListUtils.of();

        d.ce("testVariables", "//INIT");
        d.ce("testVariables", "getVarible(varName1) -> " + TS_SQLAdvVarUtils.getVarible(anchor, varName1, TS_SQLAdvVarUtils.VARTYP_STR()));
        d.ce("testVariables", "//");
        d.ce("testVariables", "//ALI GEL");
        d.ce("testVariables", "setVariable(varName1, \"ali gel\")-> " + TS_SQLAdvVarUtils.setVariable(anchor, varName1, "ali gel"));
        d.ce("testVariables", "popVaribles(ia).r-> " + TS_SQLAdvVarUtils.popVaribles(anchor, ia, o, TS_SQLAdvVarUtils.VARTYP_STR()));
        d.ce("testVariables", "cast2String(ia).o-> " + TGS_StringUtils.cmn().toString(o, ", "));
        d.ce("testVariables", "//VELI GEL");
        d.ce("testVariables", "setVariable(varName1, \"veli gel\")-> " + TS_SQLAdvVarUtils.setVariable(anchor, varName1, "veli gel"));
        d.ce("testVariables", "popVaribles(ia).r-> " + TS_SQLAdvVarUtils.popVaribles(anchor, ia, o, TS_SQLAdvVarUtils.VARTYP_STR()));
        d.ce("testVariables", "cast2String(ia).o-> " + TGS_StringUtils.cmn().toString(o, ", "));
        d.ce("testVariables", "//SET ARR STR");
        d.ce("testVariables", "setVariable(arrName1, ia)-> " + TS_SQLAdvVarUtils.setVariable(anchor, arrName1, ia));
        d.ce("testVariables", "cast2String(ia) -> " + TGS_StringUtils.cmn().toString((List) TS_SQLAdvVarUtils.getVarible(anchor, arrName1, TS_SQLAdvVarUtils.VARTYP_ARR_STR()), ", "));
        d.ce("testVariables", "//SET ARR LNG");
        d.ce("testVariables", "setVariable(arrName2, ib)-> " + TS_SQLAdvVarUtils.setVariable(anchor, arrName2, ib));
        d.ce("testVariables", "cast2String(ia) -> " + TGS_StringUtils.cmn().toString((List) TS_SQLAdvVarUtils.getVarible(anchor, arrName2, TS_SQLAdvVarUtils.VARTYP_ARR_LNG()), ", "));
    }

    public static void testFunction(TS_SQLConnAnchor anchor) {
        testFunction_createSniffFunc(anchor);
        var sql = "sniff('src','tar','id')";
        TS_SQLSelectStmtUtils.select(anchor, sql, rs -> {
            rs.walkCells(null, (ri, ci) -> {
                d.ce("testFunction", "q.ri[" + ri + "].ci[" + ci + "]:" + rs.str.get(ri, ci));
            });
        });
    }

    private static TS_SQLConnStmtUpdateResult testFunction_createSniffFunc(TS_SQLConnAnchor anchor) {
        return TGS_FuncMTCUtils.call(() -> {
            var func_intput = TGS_ListUtils.of(
                    new TGS_SQLColTyped("STR254_SRCTBLNM"),
                    new TGS_SQLColTyped("STR254_TARTBLNM"),
                    new TGS_SQLColTyped("LNG_ID")
            );
            var funcBody = new StringBuilder();
            funcBody.append("RETURN CONCAT(STR254_SRCTBLNM, '_', STR254_TARTBLNM, '-', LNG_ID)");
            return TS_SQLAdvFuncUtils.createFunction(anchor, "sniff", func_intput, new TGS_SQLColTyped("STR254_OUT"), funcBody.toString());
        });
    }
}
