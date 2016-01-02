package com.vulturegraphics.glbd;

import java.util.List;
import java.util.LinkedList;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author wraps an instance of a database table row representing a single author.
 */

public class Author extends DBObject {

    private int ID;
    private String name;
    private int alias_for;

    Author(int p_ID, String p_name, int p_alias) {
        super(p_ID);
        name = p_name;
        alias_for = p_alias;
    }

    public Author(Connection conn, int p_ID) throws SQLException {
        super(p_ID);
        final String Q_AUTHOR =
            "SELECT name,alias_for_id FROM glb_author WHERE _id=?";
        PreparedStatement st = conn.prepareStatement(Q_AUTHOR);
        st.setInt(1, p_ID);
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            name = rs.getString(1);
            alias_for = rs.getInt(2);
        }
        st.close();
    }

    @Override
    public String toString() {
        return name;
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
        Author other = (Author) obj;
        return (ID == other.ID);
    }

    public int getAlias() {
        return alias_for;
    }

    public String getName() {
        return name;
    }

    /**
     * Add an author (or alias) to the database. Author names have the form,
     *   <P>"LAST, FIRST"</P>
     *
     * @param conn The SQL connection
     * @param author_name Name of author
     * @param alias_for If <code>author_name</code> is an alias, this is the author to which the alias refers.
     *                  If provided, the referenced author must already exist. May be null.
     * @return The created Author object, or null on any failure.
     * @throws SQLException May typically occur if a uniqueness constraint fails.
     */
    public static Author addAuthor(Connection conn,
                                   String author_name,
                                   String alias_for) throws SQLException {
        Logger log = LoggerFactory.getLogger(Author.class);
        if (author_name == null) {
            return null;
        }
        final String X_AUTHOR_INSERT =
            "INSERT INTO glb_author (name, alias_for_id) VALUES (?, ?)";

        PreparedStatement st =
            conn.prepareStatement(X_AUTHOR_INSERT, Statement.RETURN_GENERATED_KEYS);

        int aliasID = 0;
        if (alias_for != null) {
            Author alias = Author.findExact(conn, alias_for);
            if (alias == null) {
                throw new SQLException("Alias for new author not found");
            }
            aliasID = alias.getID();
        }

        st.setString(1, author_name);
        st.setInt(2, aliasID);
        int rowcount = st.executeUpdate();
        if (rowcount == 0) {
            throw new SQLException("Row-id not generated for new author");
        }

        ResultSet rs = st.getGeneratedKeys();
        if (rs.next()) {
            log.info("Added author \"" + author_name + "\"");
            return new Author(rs.getInt(1), author_name, aliasID);
        }
        else {
            throw new SQLException("Could not get author's generated row-id");
        }
    }

    /**
     * Search the <code>glb_author</code> table for an <strong>exact</strong> match to an author's name.
     * @param conn The SQL Connection object.
     * @param author_name Name of author to match.
     * @return If found, the <code>Author</code> object filled in from the DB, null otherwise.
     * @throws SQLException on any error
     */
    public static Author findExact(Connection conn,
                                   String author_name) throws SQLException {
        final String Q_AUTHOR_FIND_EXACT =
            "SELECT _id,name,alias_for_id FROM glb_author WHERE name = ?";

        PreparedStatement st = conn.prepareStatement(Q_AUTHOR_FIND_EXACT);
        st.setString(1, author_name);

        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            return new Author(rs.getInt(1), rs.getString(2), rs.getInt(3));
        }
        return null;
    }

    /**
     * Find all authors that begin with the given pattern. An SQL wildcard character (<code>%</code>) is always
     * appended to the pattern and the search is done with an SQL <code>LIKE</code> qualifier. This means that the
     * following samples will all return the same list:
     * <P>ROWLING</P>
     * <P>ROWLING%</P>
     * <P>ROWLING, J.</P>
     * @param conn The SQL Connection object
     * @param author_pattern Pattern to match
     * @return A list of matching authors. If none are found, this list is empty (<code>List.size() = 0</code>).
     * @throws SQLException on any error
     * @see #findExact(Connection, String)
     */
    public static List<Author> find(Connection conn,
                                    String author_pattern) throws SQLException {
        final String Q_AUTHOR_FIND =
            "SELECT _id,name,alias_for_id FROM glb_author WHERE name LIKE ?";
        List<Author> alist = new LinkedList<>();
        if (!author_pattern.endsWith("%")) {
            author_pattern = author_pattern + "%";
        }
        PreparedStatement st = conn.prepareStatement(Q_AUTHOR_FIND);
        st.setString(1, author_pattern);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            Author a = new Author(rs.getInt(1), rs.getString(2), rs.getInt(3));
            alist.add(a);
        }
        return alist;
    }

    /**
     * A book can have many authors, an author can write many books.
     * This M:M relationship is acchieved by a mapping table,
     * <code>glb_book_author</code>, which binds a single author to a
     * single book. The same book reference can be bound to other
     * authors.
     * @param conn The SQL Connection
     * @param b The book  to add the database, associating to this Author instance
     * @return The row id of the new book entry
     * @throws SQLException Typically a constraint failure.
     */
    public int addBook(Connection conn, Book b) throws SQLException {
        final String X_BOOK_INSERT =
            "INSERT INTO glb_book_author(book_id,author_id) VALUES (?,?)";
        PreparedStatement st = conn.prepareStatement(X_BOOK_INSERT,
                                                     Statement.RETURN_GENERATED_KEYS);
        st.setInt(1, b.getID());
        st.setInt(2, this.getID());
        if (st.executeUpdate() == 0) {
            throw new SQLException("Add book failed");
        }
        else {
            ResultSet rs = st.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return 0;
    }

    /**
     * A convenience routine to find all books associated with this author.
     * @param conn The SQL Connection
     * @return A list of books by this author, possibly an empty list if no books found.
     * @throws SQLException Would be rare since you would have not been able to construct this Author
     * instance if it wasn't in the database.
     */
    public List<Book> bibliography(Connection conn) throws SQLException {
        return Book.find(conn, this);
    }

}
