package org.delcom.ifs25035springbootcashflow.config;

import org.delcom.ifs25035springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25035springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25035springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25035springbootcashflow.adapter.repository.SchemaInitializer;
import org.delcom.ifs25035springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25035springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25035springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25035springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25035springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25035springbootcashflow.usecase.UserUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    private final String uploadDir;

    public AppConfig(@Value("${app.upload-dir}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    @Bean
    public IUserRepository userRepository(DataSource dataSource) {
        SchemaInitializer.initialize(dataSource);
        return new JdbcUserRepository(dataSource);
    }

    @Bean
    public ITransactionRepository transactionRepository(DataSource dataSource,
                                                        IUserRepository userRepository) {
        // parameter userRepository memastikan tabel users dibuat lebih dulu (foreign key)
        return new JdbcTransactionRepository(dataSource);
    }

    @Bean
    public AuthUseCase authUseCase(IUserRepository userRepository) {
        return new AuthUseCase(userRepository);
    }

    @Bean
    public FinanceUseCase financeUseCase(ITransactionRepository transactionRepository) {
        return new FinanceUseCase(transactionRepository);
    }

    @Bean
    public UserUseCase userUseCase(IUserRepository userRepository) {
        return new UserUseCase(userRepository);
    }

    @Bean
    public FinancePresenter financePresenter() {
        return new FinancePresenter();
    }

    @Bean
    public FileStorage fileStorage() {
        return new FileStorage(uploadDir);
    }

    @Bean
    public ApplicationRunner seedRootAdmin(
            AuthUseCase authUseCase,
            @Value("${app.root-username}") String rootUsername,
            @Value("${app.root-password}") String rootPassword) {
        return args -> authUseCase.ensureRootAdmin(rootUsername, rootPassword);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Path.toUri() pada folder yang sudah ada otomatis berakhiran "/"
        String location = fileStorage().getRoot().toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}