package com.vulturegraphics.glbd;

public class AuthorTable extends DBTable {
    public AuthorTable() {
        super("glb_author");
    }

    @Override
    public String createTableSQL() {
        String sql_table[] = new String[] {
            "CREATE TABLE glb_author (",
            "   _id INTEGER PRIMARY KEY AUTOINCREMENT,",
            "   name TEXT NOT NULL UNIQUE,",
            "   alias_for_id INTEGER REFERENCES glb_author(_id) DEFAULT(0) ",
            ") ;"
        };
        StringBuilder sb = new StringBuilder();
        for (String s : sql_table) {
            sb.append(s + "\n");
        }
        return sb.toString();
    }
    
}
