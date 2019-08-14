package portal.api.service;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.openslice.model.Category;
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
	
	public List<Category> findAll() {
		return (List<Category>) this.categsRepo.findAll(); // findAll(new Sort(Sort.Direction.ASC, "name"));
	}
}
