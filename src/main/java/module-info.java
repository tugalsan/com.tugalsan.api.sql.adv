module com.tugalsan.api.sql.adv {
    requires com.tugalsan.api.function;
    requires com.tugalsan.api.os;
    requires com.tugalsan.api.cast;
    requires com.tugalsan.api.log;
    
    requires com.tugalsan.api.list;
    requires com.tugalsan.api.tuple;
    requires com.tugalsan.api.string;
    requires com.tugalsan.api.file.txt;
    requires com.tugalsan.api.sql.sanitize;
    requires com.tugalsan.api.sql.resultset;
    requires com.tugalsan.api.sql.select;
    requires com.tugalsan.api.sql.update;
    requires com.tugalsan.api.sql.conn;
    requires com.tugalsan.api.sql.col.typed;
    exports com.tugalsan.api.sql.adv.server;
}
