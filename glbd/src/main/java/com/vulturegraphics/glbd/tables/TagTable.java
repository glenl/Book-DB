package com.vulturegraphics.glbd;

public class TagTable extends DBTable {
    public TagTable() {
        super("glb_tag");
    }

    @Override
    public String createTableSQL() {
        String sql_table[] = new String[] {
            " CREATE TABLE " + getTableName() + " (",
            "    name TEXT PRIMARY KEY",
            "    ) WITHOUT ROWID ;"
       };
        StringBuilder sb = new StringBuilder();
        for (String s : sql_table) {
            sb.append(s + "\n");
        }
        return sb.toString();
    }
    
}
