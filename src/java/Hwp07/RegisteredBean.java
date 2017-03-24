
package Hwp07;

import java.io.IOException;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.SessionScoped;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.constraints.*;
import org.hibernate.validator.constraints.Length;
import java.lang.annotation.Target;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.validation.GroupSequence;
import org.hibernate.validator.*;



@Named(value = "regBean")
@SessionScoped
public class RegisteredBean implements Serializable{
    
    // resource injection
    @Resource(name = "jdbc/ds_wsp")
    private DataSource ds;

    @Pattern(regexp = "[a-zA-Z]+{3}", message = "Please enter a Alphabetical Username") 
    private String username;
    
    @Size(min=3, message = "Greater than 2 Characters")
    private String password;
    
    
    private String email;
    
    private String groupname;
    
    Customer c1;
    private List<Customer> customers;
    
    
    
    @Size(min=3, message = "Greater than 2 Characters")
    public String getUsername() {
        return username;
    }
    
    
    public void setUsername(String username) {
        this.username = username;
    }

    
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Pattern(regexp = "[a-zA-Z0-9]+@[uco]+\\.[edu]+", message = "Please enter an UCO email" )
    @Size(min=10, message = "Adleast 2 Chars before @uco.edu")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }    
        
    public void newMessage() throws IOException, SQLException {
        //String hitMessage = null;
        Customer c2 = new Customer();
        String group= getGroupname();
        
               
        if (ds == null) {
            throw new SQLException("ds is null; Can't get data source");
        }

        Connection conn = ds.getConnection();

        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        
        List<Customer> list = new ArrayList<>();
        
        conn.setAutoCommit(false);
        boolean committed = false;

        try 
        {
           
            //adding customer to array
            c2.setUsername(username);
            c2.setGroup(group);
            c2.setPassword(password);
            c2.setEmail(email);            
            customers.add(c2);
            
             System.out.println( " username = " + username);       
            // into usertable
            PreparedStatement ps = conn.prepareStatement(
                    "Insert into USERTABLE (username, password, email)values(?,?,?)"
            );
            
            ps.setString(1,username);
            ps.setString(2,SHA256Encrypt.encrypt(password));
            ps.setString(3,email);
                       
            //ps.executeQuery();            
            ps.executeUpdate();
            
            //into grouptable
            PreparedStatement ps2 = conn.prepareStatement(
                    "Insert into GROUPTABLE (groupname, username)values(?,?)"
            );
            
            ps2.setString(1,group);
            ps2.setString(2,username);
                                   
            //ps.executeQuery();            
            ps2.executeUpdate();
            
            conn.commit();
            conn.close();
            
        } finally {
            conn.close();
           }
        
    }
    public List<Customer> getArrayCust() throws SQLException {
    
         if (ds == null) {
            throw new SQLException("ds is null; Can't get data source");
        }

        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);
        boolean committed = false;

        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        
        List<Customer> list = new ArrayList<>();

        try 
        {
           PreparedStatement ps = conn.prepareStatement(
                    "select USERNAME, EMAIL, ID from usertable"
            );
            
            // retrieve customer data from database
            ResultSet result = ps.executeQuery();
           
            while (result.next()) {
                Customer c = new Customer();
                c.setUsername(result.getString("USERNAME"));
                c.setEmail(result.getString("EMAIL"));
                
                
                list.add(c);
                conn.commit();
                committed = true;
            }

        } finally {
            conn.close();
           }
         //System.out.println("id = " + id );
        return list;
    
    }
    
    
    public List<Customer> loadCustomers() throws SQLException {

        
        if (ds == null) {
            throw new SQLException("ds is null; Can't get data source");
        }

        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);
        boolean committed = false;

        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        
        List<Customer> list = new ArrayList<>();
        List<Customer> userList = new ArrayList<>();
        userList = getArrayCust();
        List<Customer> totalList = new ArrayList<>();
        try 
        {
           PreparedStatement ps = conn.prepareStatement(
                    "select GROUPNAME, USERNAME from grouptable"
            );
            
            // retrieve customer data from database
            ResultSet result = ps.executeQuery();
           
            while (result.next()) {
                Customer c = new Customer();
                c.setGroup(result.getString("GROUPNAME"));
                c.setUsername(result.getString("USERNAME"));
                
               list.add(c);
                conn.commit();
                committed = true;
            }

        } finally {
            conn.close();
           }
        
        for(Customer cust1: list )
        {            
            for(Customer cust2: userList )
                
                if(cust1.username.equals(cust2.username))
                {
                    cust1.setEmail(cust2.email);
                }

            totalList.add(cust1);
        
        }
        
        // combines two tables
        List<Customer> finalList = new ArrayList<>();
        for(Customer cust1: totalList)
        {
            int counter = 0;
            for(Customer cust2: totalList)
            {
                if(cust1.username.equals(cust2.username))
                {
                    counter++;
                   if(counter == 2) 
                   {
                       cust1.setGroup("customergroup, admingroup");
                   }
                }
            
            }
        
        finalList.add(cust1);
        
        }
        
        
        // removes duplicates form table
        for(int i=0;i<finalList.size();i++){
            for(int j=i+1;j<finalList.size();j++){
                if(finalList.get(i).username.equals(finalList.get(j).username)){
                    finalList.remove(j);
                    j--;
                }   
            }
        }
        
        //System.out.println("id = " + id );
        return finalList;
    }
        
    @PostConstruct
    public void init(){
        username = null;
        password = null;
        email = null;
        groupname = "customergroup";
        try {
            customers = loadCustomers();
        } catch (SQLException ex) {
            Logger.getLogger(UserBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        c1 = new Customer();
        
    }
    
    
    
}
