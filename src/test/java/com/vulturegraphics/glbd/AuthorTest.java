package com.vulturegraphics.glbd;

import java.util.List;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import junit.framework.TestCase;

public class AuthorTest extends TestCase{

    public void testSingle() throws SQLException {
        String path = Config.getInstance().getProperty("glbd.dbpath");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + path);
        conn.setAutoCommit(false);
        Author author = Author.findExact(conn, "Niven, Larry");
        assertTrue(author != null);
    }

    public void testMultiple() throws SQLException {
        String path = Config.getInstance().getProperty("glbd.dbpath");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + path);
        conn.setAutoCommit(false);
        List<Author> alist = Author.find(conn, "A");
        assertTrue(alist != null);
        assertTrue(alist.size() > 1);
    }
}
