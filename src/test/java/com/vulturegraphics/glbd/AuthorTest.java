package com.vulturegraphics.glbd;


import java.util.List;
import java.lang.InterruptedException;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteQueue;
import com.almworks.sqlite4java.SQLiteJob;
import junit.framework.TestCase;

public class AuthorTest extends TestCase{

    public void testSingle() throws SQLiteException, InterruptedException {
        SQLiteQueue q = DBase.getInstance().getQueue();
        Author a = q.execute(new SQLiteJob<Author>() {
                protected Author job(SQLiteConnection conn) throws SQLiteException {
                    List<Author> alist = Author.find(conn, "Rowling");
                    if (alist.size() < 1) {
                        return null;
                    }
                    return alist.get(0);
                }
            }).complete();
        q.stop(true).join();
        assertTrue(a != null && a.toString().length() > 0);
    }

    public void testMultiple() throws SQLiteException, InterruptedException {
        SQLiteQueue q = DBase.getInstance().getQueue();
        List<Author> alist = q.execute(new SQLiteJob< List<Author> >() {
                protected List<Author> job(SQLiteConnection conn) throws SQLiteException {
                    return Author.find(conn, "A");
                }
            }).complete();
        q.stop(true).join();
        assertTrue(alist != null);
        assertTrue(alist.size() > 1);
    }
}
