/*-
 * ========================LICENSE_START=================================
 * io.openslice.portal.api
 * %%
 * Copyright (C) 2019 openslice.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package portal.api.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.openslice.model.Product;
import portal.api.repo.ProductRepository;

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
