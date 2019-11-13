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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.openslice.model.Category;
import io.openslice.model.PortalUser;
import io.openslice.model.UserRoleType;
import portal.api.repo.CategoriesRepository;


/**
 * @author ctranoris
 *
 */
@Service
public class CategoryService {


	@Autowired
	CategoriesRepository categsRepo;
	

	private static final transient Log logger = LogFactory.getLog( CategoryService.class.getName() );

	
	public CategoryService() {
		super();
		
	}
	
	@PostConstruct
	public void initRepo() {
		
		Category c = null;
		try {
			c = findById(1);
			logger.info("======================== catgegory  = " + c);
		} catch (Exception e) {
			logger.info("======================== catgegory NOT FOUND, initializing");			
		}

		if (c == null) {

			c = new Category();
			c.setName("None");
			categsRepo.save( c );
			c = new Category();
			c.setName("Networking");
			categsRepo.save( c );
			c = new Category();
			c.setName("Service");
			categsRepo.save( c );

		}		
	}
	
	public List<Category> findAll() {
		return (List<Category>) this.categsRepo.findAll(); // findAll(new Sort(Sort.Direction.ASC, "name"));
	}

	public Category addCategory(Category c) {
		return this.categsRepo.save( c );
	}

	public Category findById(long catid) {
		Optional<Category> optionalCat = this.categsRepo.findById( catid );
		return optionalCat
				.orElse(null);
	}

	public Category updateCategoryInfo(Category c) {
		return this.categsRepo.save( c );
	}

	public void deleteCategory(Category c) {
		this.categsRepo.delete(c);
		
	}
}
