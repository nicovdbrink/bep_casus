package sql.dao;

import sql.models.Customer;
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


public class CustomerDAO extends BaseDao implements ICustomerDAO{
    private static final Logger logger = LoggerFactory.getLogger(CustomerDAO.class);
    private static CustomerDAO sinlgeInstance;

    public static CustomerDAO getInstance() {
        if (sinlgeInstance == null)
        {
            sinlgeInstance = new CustomerDAO();
        }
        return sinlgeInstance;
    }

    public Customer selectCustomerInformation(int customerId) {

        List<Customer> customers = new ArrayList<>();

        try (Connection connection = super.getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT * FROM bifi.Persoon AS persoon, bifi.Klant AS klant, bifi.Adres AS adres WHERE persoon.klantid = ? AND adres.klantid = ? AND klant.klantid = ? AND adres.type = 'F'")) {

            stmt.setInt(1, customerId);
            stmt.setInt(2, customerId);
            stmt.setInt(3, customerId);

            customers.add(createInformation(stmt));


        } catch(SQLException ex) {
            logger.info("XXXXXXXXXXXXXXXXXX ERROR WHILE EXECUTING TO STATEMENT XXXXXXXXXXXXXXXXXXXXXXXXXX");
            logger.info(ex.getMessage(), ex);
        }

        return customers.get(0);
    }

    private Customer createInformation(PreparedStatement stmt) {
        AdressMaker adressMaker;
        Salutation salutation;
        String iban;
        Customer customer = null;

        try(ResultSet rs = stmt.executeQuery()) {
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

                    customer = new Customer(name, adress, bank);
                }
                else {
                    String companyName = rs.getString("bedrijfsnaam");
                    String vatNumber = rs.getString("vat");

                    Company company = new Company(companyName, vatNumber, adress, bank);
                    customer = new Customer(name, adress, bank, company);
                }
            }
        } catch (SQLException ex) {
            logger.info("XXXXXXXXXXXXXXXXXX ERROR WHILE EXECUTING TO STATEMENT XXXXXXXXXXXXXXXXXXXXXXXXXX");
            logger.info(ex.getMessage(), ex);
        }

        return customer;
    }
}