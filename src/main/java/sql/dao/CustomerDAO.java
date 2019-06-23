package sql.dao;

import sql.models.Customer;
import sql.connector.DBConnector;
import sql.models.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import enums.Enums.*;

import sql.models.Adress;
import sql.models.Bank;
import sql.models.Company;


public class CustomerDAO {
    private DBConnector connector = DBConnector.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(CustomerDAO.class);

    public List<Customer> selectCustomerInformation(int customerId, String adressType) {
        AdressMaker adressMaker;
        Connection connection = connector.getConnection();
        List<Customer> customers = new ArrayList<>();
        Salutation salutation;
        String iban;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = connection.prepareStatement("SELECT * FROM bifi.Persoon AS persoon, bifi.Klant AS klant, bifi.Adres AS adres WHERE persoon.klantid = ? AND adres.klantid = ? AND klant.klantid = ? AND adres.type = ?");
            stmt.setInt(1, customerId);
            stmt.setInt(2, customerId);
            stmt.setInt(3, customerId);
            stmt.setString(4, adressType);
            rs = stmt.executeQuery();

            while (rs.next()) {
                adressMaker = new AdressMaker(rs.getString("straat"), rs.getString("plaats"), rs.getString("huisnummer"), rs.getString("postcode"));

                String firstName = rs.getString("voornaam");
                String lastName = rs.getString("achternaam");
                String middleName = rs.getString("tussenvoegsel");
                String street = adressMaker.getStreet();
                String houseNumber = adressMaker.getHouseNumber();
                String postalcode = adressMaker.getPostalcode();
                String city = adressMaker.getCity();

                String bic = rs.getString("bic");


                if (rs.getString("geslacht").equals("m")) {
                    salutation = Salutation.DHR;
                }
                else {
                    salutation = Salutation.MVR;
                }

                if (rs.getString("bankrek") != null) {
                    iban = rs.getString("bankrek");
                }
                else {
                    iban = rs.getString("giro");
                }

                Name name = new Name(salutation, firstName, lastName, middleName);
                Bank bank = new Bank(iban, bic);
                Adress adress = new Adress(street, postalcode, city, houseNumber);
                if (rs.getString("bedrijfsnaam") == null) {

                    Customer customer = new Customer(name, adress, bank);
                    customers.add(customer);
                }
                else {
                    String companyName = rs.getString("bedrijfsnaam");
                    String btwNumber = rs.getString("vat");

                    Company company = new Company(companyName, btwNumber, adress, bank);
                    Customer customer = new Customer(name, adress, bank, company);
                    customers.add(customer);
                }
            }
        }catch(SQLException ex) {
            logger.info("XXXXXXXXXXXXXXXXXX ERROR WHILE EXECUTING TO STATEMENT XXXXXXXXXXXXXXXXXXXXXXXXXX");
            logger.info(ex.getMessage(), ex);
        }
        finally {
            try {
                rs.close();
                stmt.close();
                connection.close();
            } catch (SQLException e) {
                logger.info("XXXXXXXXXXXXXXXXXX ERROR WHILE CLOSING CONNECTION XXXXXXXXXXXXXXXXXXXXXXXXXX");
                logger.info(e.getMessage(), e);
            }

        }

        return customers;
    }
}
