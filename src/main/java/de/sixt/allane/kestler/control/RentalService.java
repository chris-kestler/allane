package de.sixt.allane.kestler.control;

import de.sixt.allane.kestler.model.Contract;
import de.sixt.allane.kestler.model.Customer;
import de.sixt.allane.kestler.model.Identifiable;
import de.sixt.allane.kestler.model.Vehicle;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.core.env.Environment;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

/**
 * REST Service used to retrieve data from the database
 */
@Service
public class RentalService {
    private final SessionFactory sessionFactory;

    @Autowired
    private Environment env;

    @Autowired
    public RentalService(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Generic function to retrieve a single row from a table using a given Key
     *
     * @param type The POJO model class
     * @param key the unique key for that class. This function does not support compisite keys
     * @return a pojo for that row
     */
    public <K extends Serializable, T extends Identifiable<K>> T retrieve(Class<T> type, K key){
        if(key == null)
            return null;
        try (Session session = sessionFactory.openSession()) {
            return session.get(type, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public <T> List<T> retrieveAll(Class<T> type){
        try (Session session = sessionFactory.openSession()) {
            Query<T> query = session.createQuery("FROM "+type.getName(), type);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets a list of all contracts; and also populates the vehicle and customer data
     *
     * There was some copy-pasta from retrieve all, but this allows the IDE to check my queries
     */
    public List<Contract> retrieveAllContracts(){
        try (Session session = sessionFactory.openSession()) {
            Query<Contract> query = session.createQuery("SELECT c FROM Contract c LEFT JOIN FETCH c.vehicleData LEFT JOIN FETCH c.customerData", Contract.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets a set of vehicles that are used by contracts
     */
    public Collection<Long> retrieveUsedVehicles(){
        try (Session session = sessionFactory.openSession()) {
            Query<Vehicle> query = session.createQuery("SELECT DISTINCT c.vehicleData FROM Contract c", Vehicle.class);
            HashSet<Long> ids = new HashSet<>();
            query.list().forEach((vehicle -> ids.add(vehicle.getId())));
            return ids;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieve a single contract and populate the customer and vehicle data
     *
     * @param id The id of that contract
     * @return the contract
     */
    public Contract retrieveContract(long id){
        try (Session session = sessionFactory.openSession()) {
            Query<Contract> query = session.createQuery("SELECT c FROM Contract c LEFT JOIN FETCH c.vehicleData LEFT JOIN FETCH c.customerData WHERE c.id=:cid", Contract.class);
            query.setParameter("cid", id);
            return query.list().get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> T save(T t){
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.merge(t);
            session.getTransaction().commit();
            return t;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public <K extends Serializable, T extends Identifiable<K>> boolean delete(Class<T> t, K k) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            var created = t.getDeclaredConstructor().newInstance();
            created.setId(k);
            session.delete(created);

            session.getTransaction().commit();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}