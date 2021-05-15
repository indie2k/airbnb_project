package airbnb;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="messages", path="messages")
public interface MessageRepository extends PagingAndSortingRepository<Message, Long>{


}
