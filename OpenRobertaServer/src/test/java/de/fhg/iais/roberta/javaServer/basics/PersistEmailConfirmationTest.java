package de.fhg.iais.roberta.javaServer.basics;

import java.util.List;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.fhg.iais.roberta.persistence.bo.PendingEmailConfirmations;
import de.fhg.iais.roberta.persistence.bo.Role;
import de.fhg.iais.roberta.persistence.bo.User;
import de.fhg.iais.roberta.persistence.dao.PendingEmailConfirmationsDao;
import de.fhg.iais.roberta.persistence.dao.UserDao;
import de.fhg.iais.roberta.persistence.util.DbSession;
import de.fhg.iais.roberta.persistence.util.DbSetup;
import de.fhg.iais.roberta.persistence.util.SessionFactoryWrapper;
import de.fhg.iais.roberta.util.dbc.Assert;

public class PersistEmailConfirmationTest {
    private SessionFactoryWrapper sessionFactoryWrapper;
    private DbSetup memoryDbSetup;
    private String connectionUrl;
    private Session nativeSession;
    private DbSession hSession;
    private UserDao userDao;
    private PendingEmailConfirmationsDao confirmationsDao;

    private static final int TOTAL_USERS = 5;

    @Before
    public void setup() throws Exception {
        this.connectionUrl = "jdbc:hsqldb:mem:passwordInMemoryDb";
        this.sessionFactoryWrapper = new SessionFactoryWrapper("hibernate-test-cfg.xml", this.connectionUrl);
        this.nativeSession = this.sessionFactoryWrapper.getNativeSession();
        this.memoryDbSetup = new DbSetup(this.nativeSession);
        this.memoryDbSetup.runDefaultRobertaSetup();

        this.hSession = this.sessionFactoryWrapper.getSession();
        this.userDao = new UserDao(this.hSession);
        this.confirmationsDao = new PendingEmailConfirmationsDao(this.hSession);

        //Create list of users
        for ( int userNumber = 0; userNumber < PersistEmailConfirmationTest.TOTAL_USERS; userNumber++ ) {
            User user = this.userDao.loadUser("account-" + userNumber);
            if ( user == null ) {
                User user2 = new User("account-" + userNumber);
                user2.setEmail("stuff-" + userNumber);
                user2.setPassword("pass-" + userNumber);
                user2.setRole(Role.STUDENT);
                user2.setTags("rwth");
                this.hSession.save(user2);
                this.hSession.commit();
            }
        }
    }

    @Test
    public void createUrls() throws Exception {
        List<User> userList = this.userDao.loadUserList("created", 0, "rwth");
        Assert.isTrue(userList.size() == 5);

        for ( int userNumber = 0; userNumber < PersistEmailConfirmationTest.TOTAL_USERS; userNumber++ ) {
            User user = this.userDao.loadUser("account-" + userNumber);
            PendingEmailConfirmations confirmation = this.confirmationsDao.loadConfirmationByUser(user.getId());
            if ( confirmation == null ) {
                PendingEmailConfirmations confirmation2 = new PendingEmailConfirmations(user.getId());
                this.hSession.save(confirmation2);
                this.hSession.commit();
            }
        }
        Assert.notNull(this.confirmationsDao.get(2));
        Assert.isNull(this.confirmationsDao.get(8));
    }

    @Test
    public void activateAccount() throws Exception {
        User user2 = this.userDao.loadUserByEmail("stuff-2");
        Assert.notNull(user2);
        Assert.isTrue(user2.getAccount().equals("account-2"));
        PendingEmailConfirmations confirmation = this.confirmationsDao.persistConfirmation(user2.getId());
        Assert.notNull(confirmation);
        this.hSession.save(confirmation);
        this.hSession.commit();

        User user = this.userDao.get(confirmation.getUserID());
        if ( user != null ) {
            user.setActivated(true);
            this.hSession.save(user);
            this.hSession.commit();
        }
        User userChanged = this.userDao.loadUser("account-2");
        Assert.isTrue(userChanged.isActivated());
    }

    @After
    public void tearDown() {
        this.memoryDbSetup.deleteAllFromUserAndProgramTmpPasswords();
    }

}
