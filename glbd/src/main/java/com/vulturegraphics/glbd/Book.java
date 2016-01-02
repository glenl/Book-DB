package com.vulturegraphics.glbd;

import java.util.List;
import java.util.LinkedList;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A Book wraps a row in a database table representing a single book. Duplicates are not
 * checked since two authors may write a book with the same title.
 */
public class Book extends DBObject {
    private String title;

    Book(int p_ID, String p_title) {
        super(p_ID);
        title = p_title;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Book other = (Book) obj;
        return (ID == other.ID && title.equals(other.title));
    }

    @Override
    public String toString() {
        return title;
    }

    /**
     * Add a title to the Book table
     * @param conn The connection object
     * @param title String title to add to database table
     * @return Book object
     * @throws SQLException on any error
     */
    public static Book addTitle(Connection conn,
                                String title) throws SQLException {
        final String X_BOOK_INSERT = "INSERT INTO glb_book (title) VALUES(?)";
        PreparedStatement st =
            conn.prepareStatement(X_BOOK_INSERT, Statement.RETURN_GENERATED_KEYS);
        st.setString(1, title);
        if (st.executeUpdate() == 0) {
            throw new SQLException("Row-id not generated for new book");
        }
        ResultSet rs = st.getGeneratedKeys();
        if (rs.next()) {
            return new Book(rs.getInt(1), title);
        }
        return null;
    }

    /**
     * Find books by a given author
     * @param conn The connection object
     * @param author Find books written by this author
     * @return List of books
     * @throws SQLException on any error
     */
    public static List<Book> find(Connection conn, Author author) throws SQLException {
        final String Q_BOOK_FIND = "SELECT"
            + " _id,title FROM glb_book WHERE _id IN"
            + " (SELECT book_id FROM glb_book_author WHERE author_id=?)";
        List<Book> bookList = new LinkedList<>();
        PreparedStatement st = conn.prepareStatement(Q_BOOK_FIND);
        st.setInt(1, author.getID());
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            Book b = new Book(rs.getInt(1), rs.getString(2));
            bookList.add(b);
        }
        return bookList;
    }
}
