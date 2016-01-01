package com.vulturegraphics.glbd;

import java.util.List;
import java.util.ArrayList;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.io.IOException;
import org.junit.*;

public class AuthorTest {
    static Connection _conn;

    /** Build a mock in-memory database for testing.
     *  This defines all the tables to test and populates a few
     *  authors in the glb_author table.
     */
    @BeforeClass
    static public void setUp() {
        try {
            _conn = DriverManager.getConnection("jdbc:sqlite::memory:");
            _conn.setAutoCommit(false);
            List<DBTable> tbl = new ArrayList(3);
            tbl.add(new AuthorTable());
            tbl.add(new BookTable());
            tbl.add(new BookAuthorTable());
            System.out.println("building tables");
            for (DBTable dbt : tbl) {
                dbt.makeTable(_conn);
            }
            _conn.commit();
            for (DBTable dbt : tbl) {
                if (dbt.populateFromDDL(_conn)) {
                    System.out.println("populated " + dbt.getTableName());
                }
            }
            _conn.commit();
        }
        catch (IOException|SQLException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    static public void tearDown() {
        try {
            _conn.close();
        }
        catch ( SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void can_add_author() throws SQLException {
        Author a = Author.addAuthor(_conn, "Smith, Joe", null);
        Assert.assertNotNull(a);
        Assert.assertNotEquals(0, a.getID());
        _conn.commit();
    }

    @Test (expected = SQLException.class)
    public void throws_exception_on_duplicate_author() throws SQLException {
        Author.addAuthor(_conn, "Huxley, Aldous", null);
    }

    @Test
    public void can_find_author() throws SQLException {
        Author author = Author.findExact(_conn, "Huxley, Aldous");
        Assert.assertNotNull(author);
    }

    @Test
    public void allows_duplicate_titles() throws SQLException {
        Book.addTitle(_conn, "This is only a Test");
        Book.addTitle(_conn, "This is only a Test");
        _conn.rollback();
    }

    @Test
    public void can_find_multiple() throws SQLException {
        List<Author> authors = Author.find(_conn, "H");
        Assert.assertEquals(2, authors.size());
    }

    @Test
    public void can_add_book() throws SQLException {
        Author author = Author.findExact(_conn, "Huxley, Aldous");
        Assert.assertNotNull(author);
        Book book = Book.addTitle(_conn, "Eyeless in Gaza");
        Assert.assertNotNull(book);
        Assert.assertNotEquals(0, author.addBook(_conn, book));
        _conn.commit();
    }

}
