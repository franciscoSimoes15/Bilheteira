package ConnectDatabase;

import java.sql.*;
import java.util.Scanner;

public class ConnDB
{ // formato da data muito importante para nao dar erro
    // YYYY-MM-dd HH:mm:ss.sss
    private String DATABASE_URL;
    private Connection dbConn;

    private int versao;

    public ConnDB(String database_dir) throws SQLException
    {
        DATABASE_URL = database_dir;
        dbConn = DriverManager.getConnection(DATABASE_URL);
        versao = 1;
        clear();
    }

    public void close() throws SQLException
    {
        if (dbConn != null)
            dbConn.close();
    }

    public void listUsers(String whereName) throws SQLException
    {
        Statement statement = dbConn.createStatement();

        String sqlQuery = "SELECT id, name, birthdate FROM users";
        if (whereName != null)
            sqlQuery += " WHERE name like '%" + whereName + "%'";

        ResultSet resultSet = statement.executeQuery(sqlQuery);

        while(resultSet.next())
        {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            Date birthdate = resultSet.getDate("birthdate");
            System.out.println("[" + id + "] " + name + " (" + birthdate + ")");
        }

        resultSet.close();
        statement.close();
    }

    public void insertUser(String name, String birthdate) throws SQLException
    {
        Statement statement = dbConn.createStatement();

        String sqlQuery = "INSERT INTO users VALUES (NULL,'" + name + "','" + birthdate + "')";
        statement.executeUpdate(sqlQuery);
        statement.close();
    }

    public void updateUser(int id, String name, String birthdate) throws SQLException
    {
        Statement statement = dbConn.createStatement();

        String sqlQuery = "UPDATE users SET name='" + name + "', " +
                "BIRTHDATE='" + birthdate + "' WHERE id=" + id;
        statement.executeUpdate(sqlQuery);
        statement.close();
    }

    public void deleteUser(int id) throws SQLException
    {
        Statement statement = dbConn.createStatement();

        String sqlQuery = "DELETE FROM users WHERE id=" + id;
        statement.executeUpdate(sqlQuery);
        statement.close();
    }

    public void clear() throws SQLException
    {
        Statement statement = dbConn.createStatement();
        String sqlQuery = "DELETE FROM espetaculo";
        statement.executeUpdate(sqlQuery);
        statement.close();

        //Getting the connection
        /*System.out.println("Connection established......");
        ResultSet rs = dbConn.getMetaData().getTables(null, null, null, null);        while (rs.next()) {
            System.out.println(rs.getString("TABLE_NAME"));
        }*/
    }

    public int getVersao() {
        return versao;
    }

    public void setVersao(int versao) {
        this.versao = versao;
    }


    /*public static void main(String[] args)
    {
        try
        {
            ConnDB connDB = new ConnDB();
            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            while (!exit)
            {
                System.out.print("Command: ");
                String command = scanner.nextLine();
                String[] comParts = command.split(",");

                if (command.startsWith("select"))
                    connDB.listUsers(null);
                else if (command.startsWith("find"))
                    connDB.listUsers(comParts[1]);
                else if (command.startsWith("insert"))
                    connDB.insertUser(comParts[1], comParts[2]);
                else if (command.startsWith("update"))
                    connDB.updateUser(Integer.parseInt(comParts[1]), comParts[2], comParts[3]);
                else if (command.startsWith("delete"))
                    connDB.deleteUser(Integer.parseInt(comParts[1]));
                else
                    exit = true;
            }

            connDB.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }*/
}
