package glbd;

import org.junit.*;
import java.util.List;
import java.lang.InterruptedException;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteQueue;
import com.almworks.sqlite4java.SQLiteJob;

public class AuthorTest {
    
    @Test
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
        Assert.assertTrue(a != null && a.toString().length() > 0);
    }

    @Test
    public void testMultiple() throws SQLiteException, InterruptedException {
        SQLiteQueue q = DBase.getInstance().getQueue();
        List<Author> alist = q.execute(new SQLiteJob< List<Author> >() {
                protected List<Author> job(SQLiteConnection conn) throws SQLiteException {
                    return Author.find(conn, "A");
                }
            }).complete();
        q.stop(true).join();
        Assert.assertTrue(alist != null);
        Assert.assertTrue(alist.size() > 1);
    }
}
