package com.tugalsan.api.sql.adv.server;

import java.util.*;
import com.tugalsan.api.log.server.*;
import com.tugalsan.api.sql.col.typed.client.*;
import com.tugalsan.api.sql.conn.server.*;
import com.tugalsan.api.sql.sanitize.server.*;
import com.tugalsan.api.sql.update.server.*;
import com.tugalsan.api.string.client.*;
import java.util.stream.*;

public class TS_SQLAdvFuncUtils {

    final private static TS_Log d = TS_Log.of(TS_SQLAdvFuncUtils.class);

    //TODO createFunction test: EXAMPLE BODY1: "RETURN CONCAT(p1, ' ', p2)"
    //TODO createFunction test: EXAMPLE BODY2: 
    //  DECLARE NAME_FOUND VARCHAR DEFAULT "";
    //  SELECT EMPLOYEE_NAME INTO NAME_FOUND FROM TABLE_NAME WHERE ID = PID;
    //  RETURN NAME_FOUND;
    //TODO createFunction test: EXAMPLE BODY3;
    //    CREATE FUNCTION no_of_years(date1 date) RETURNS int DETERMINISTIC
    //    BEGIN
    //     DECLARE date2 DATE;
    //      Select current_date()into date2;
    //      RETURN year(date2)-year(date1);
    //    END 
    public static TS_SQLConnStmtUpdateResult createFunction(TS_SQLConnAnchor anchor, CharSequence functionName,
            List<TGS_SQLColTyped> inputParams, TGS_SQLColTyped output, CharSequence body) {
        TS_SQLSanitizeUtils.sanitize(functionName);
        TS_SQLSanitizeUtils.sanitize(inputParams);
        TS_SQLSanitizeUtils.sanitize(output);
        var sql = "DROP FUNCTION IF EXISTS " + functionName;
        var r = TS_SQLUpdateStmtUtils.update(anchor, sql);
        d.ci("createFunction.INFO: Connection.createFunction.sql0: ", sql, ", r:", r);

        var sb = new StringBuilder();
        sb.append("CREATE FUNCTION ").append(functionName).append('(');

        //SETTING INPUT TYPE
        IntStream.range(r.affectedRowCount, inputParams.size()).forEachOrdered(i -> {
            sb.append(inputParams.get(i).toString());
            sb.append(' ');
            sb.append(TS_SQLConnColUtils.creationType(anchor, inputParams.get(i)));
            if (i != inputParams.size() - 1) {
                sb.append(", ");
            }
        });
        sb.append(')').append(' ');

        //SETTING OUTPUT TYPE
        sb.append("RETURNS ");//USE UnDETERMINISTIC TO BE SAFE
        sb.append(TS_SQLConnColUtils.creationType(anchor, output));
        sb.append('\n');

        //BODY 
        sb.append("BEGIN\n").append(body).append("\nEND\n");

        //RUN
        sql = sb.toString();
        r = TS_SQLUpdateStmtUtils.update(anchor, sql);
        d.ci("createFunction.INFO: Connection.createFunction.sql2: ", sql + ", r:", r);
        return r;
    }

    public static TS_SQLConnStmtUpdateResult deleteFunction(TS_SQLConnAnchor anchor, CharSequence functionName) {
        TS_SQLSanitizeUtils.sanitize(functionName);
        var sql = TGS_StringUtils.cmn().concat("DROP FUNCTION ", functionName);
        d.ci("deleteFunction.INFO: Connection.deleteFunction.sql: ", sql);
        var r = TS_SQLUpdateStmtUtils.update(anchor, sql);
        d.ci("deleteFunction.INFO: Connection.deleteFunction.r: ", r);
        return r;
    }
}
