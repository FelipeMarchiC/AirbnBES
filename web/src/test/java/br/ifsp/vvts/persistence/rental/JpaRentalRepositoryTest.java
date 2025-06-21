package br.ifsp.vvts.persistence.rental;

import br.ifsp.application.property.repository.JpaPropertyRepository;
import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.user.repository.JpaUserRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@RequiredArgsConstructor
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JpaRentalRepositoryTest {
    @Autowired
    protected JpaUserRepository userRepository;
    @Autowired
    protected JpaPropertyRepository propertyRepository;
    @Autowired
    protected JpaRentalRepository sut;
    protected static final Faker faker = new Faker();
}
