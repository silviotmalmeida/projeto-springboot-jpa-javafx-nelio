package com.silviotmalmeida.app.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.silviotmalmeida.app.entities.Seller;
import com.silviotmalmeida.app.repositories.SellerRepository;
import com.silviotmalmeida.app.services.exceptions.DatabaseException;
import com.silviotmalmeida.app.services.exceptions.ResourceNotFoundException;

// classe de serviço que realiza a comunicação entre o SellerListController e o SellerRepository
// registrando a classe como service
@Service
public class SellerService {

    // injetando o repository da entidade Seller
    @Autowired
    private SellerRepository repository;

    // método que retorna todos os registros
    public List<Seller> findAll() {

        return this.repository.findAll();
    }

    // método que retorna o registro do id selecionado
    public Seller findById(Long id) {

        // obtendo o registro
        Optional<Seller> obj = this.repository.findById(id);

        // retorna o registro, se existir
        return obj.get();
    }

    // método que insere um registro no BD
    public Seller insert(Seller obj) {

        // retorna o registro, após a inserção
        return this.repository.save(obj);
    }

    // método que edita um registro no BD
    public Seller update(Long id, Seller obj) {

        // preparando um objeto monitorado
        // é mais performático do que o findById
        Optional<Seller> entity = this.repository.findById(id);

        // se o registro não for encontrado, lança uma exceção
        if (entity.isEmpty()) {
            throw new ResourceNotFoundException(id);
        }

        // editando os atributos permitidos
        updateData(entity.get(), obj);

        // retorna o registro, após a edição
        return this.repository.save(entity.get());
    }

    // método auxiliar para realizar a edição de atributos permitidos de um registro
    private void updateData(Seller entity, Seller obj) {

        // editando os atributos permitidos
        entity.setName(obj.getName());
        entity.setEmail(obj.getEmail());
        entity.setBirthDate(obj.getBirthDate());
        entity.setBaseSalary(obj.getBaseSalary());
        entity.setDepartment(obj.getDepartment());
    }

    // método que insere ou edita um registro
    public void saveOrUpdate(Seller obj) {

        // se o objeto já possuir id, trata-se de create
        if (obj.getId() == null) {

            this.insert(obj);
        }
        // senão, trata-se de update
        else {

            this.update(obj.getId(), obj);
        }
    }

    // método que remove um registro no BD
	public void delete(Long id) {

		// tratando exceções
		try {
			// retorna o registro, após a remoção
			this.repository.deleteById(id);
		}
		// em caso de id não encontrado, lança a exceção personalizada
		catch (EmptyResultDataAccessException e) {
			throw new ResourceNotFoundException(id);
		}
		// em caso de id com pedidos, lança a exceção personalizada
		catch (DataIntegrityViolationException e) {
			throw new DatabaseException("Database integrity violation.");
		}
	}
}
