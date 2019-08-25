package portal.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.openslice.model.ExperimentMetadata;
import io.openslice.model.MANOprovider;
import io.openslice.model.Product;
import io.openslice.model.VxFMetadata;
import portal.api.repo.ManoProvidersRepository;
import portal.api.repo.ProductRepository;
import portal.api.repo.VxFsRepository;

@Service
public class ProductService {

	@Autowired
	ProductRepository productsRepo;

	public Product getProductByID(long id) {

		Optional<Product> o = this.productsRepo.findById(id);

		return o.orElse(null);
	}

	public Product updateProductInfo(Product prevProduct) {
		return this.productsRepo.save( prevProduct ) ;
	}

	public Product getProducttByUUID(String uuid) {

		Optional<Product> o = this.productsRepo.findByUUID(uuid);

		return o.orElse(null);
	}


}
