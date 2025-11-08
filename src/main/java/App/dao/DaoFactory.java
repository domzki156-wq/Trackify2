package App.dao;

public class DaoFactory {
    private static TransactionDao transactionDao;
    private static UserDao userDao;
    private static ProductDao productDao;

    public static synchronized TransactionDao getTransactionDao() {
        if (transactionDao == null) {
            transactionDao = new MongoTransactionDao();
        }
        return transactionDao;
    }

    public static synchronized UserDao getUserDao() {
        if (userDao == null) {
            userDao = new MongoUserDao();
        }
        return userDao;
    }

    public static synchronized ProductDao getProductDao() {
        if (productDao == null) {
            productDao = new MongoProductDao();
        }
        return productDao;
    }

    public static synchronized void setTransactionDao(TransactionDao dao) {
        transactionDao = dao;
    }

    public static synchronized void setUserDao(UserDao dao) {
        userDao = dao;
    }

    public static synchronized void setProductDao(ProductDao dao) {
        productDao = dao;
    }
}
