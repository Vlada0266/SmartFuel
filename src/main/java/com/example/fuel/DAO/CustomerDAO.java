package com.example.fuel.DAO;

import com.example.fuel.model.Customer;
import java.util.List;

/**
 * DAO-интерфейс для клиентов.
 */
public interface CustomerDAO {
    Customer getById(int id);
    List<Customer> getAll();
    void update(Customer customer);
}
