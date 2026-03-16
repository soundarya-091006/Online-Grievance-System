package com.safereport.config;

import com.safereport.entity.Category;
import com.safereport.entity.User;
import com.safereport.enums.Role;
import com.safereport.repository.CategoryRepository;
import com.safereport.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedAuthority();
        seedCategories();
    }

    private void seedAdmin() {
        if (!userRepository.existsByEmail("admin@safereport.com")) {
            User admin = User.builder()
                    .fullName("System Admin")
                    .email("admin@safereport.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .active(true)
                    .emailVerified(true)
                    .build();
            userRepository.save(admin);
            log.info("✅ Admin seeded: admin@safereport.com / Admin@123");
        }
    }

    private void seedAuthority() {
        if (!userRepository.existsByEmail("authority@safereport.com")) {
            User authority = User.builder()
                    .fullName("Authority Officer")
                    .email("authority@safereport.com")
                    .password(passwordEncoder.encode("Auth@123"))
                    .role(Role.AUTHORITY)
                    .active(true)
                    .emailVerified(true)
                    .build();
            userRepository.save(authority);
            log.info("✅ Authority seeded: authority@safereport.com / Auth@123");
        }
    }

    private void seedCategories() {
        if (!categoryRepository.existsByName("Domestic Violence")) {
            List<Category> newCats = List.of(
                Category.builder().name("Domestic Violence").description("Abuse or violence by a partner or family member").icon("🏠").active(true).build(),
                Category.builder().name("Workplace Harassment").description("Unwelcome conduct or harassment at work").icon("🏢").active(true).build(),
                Category.builder().name("Cyberbullying").description("Online harassment, stalking, or threats").icon("💻").active(true).build(),
                Category.builder().name("Public Harassment").description("Eve teasing, stalking, or harassment in public spaces").icon("🚌").active(true).build(),
                Category.builder().name("Sexual Assault").description("Any unwanted sexual contact or assault").icon("⚠️").active(true).build(),
                Category.builder().name("Stalking").description("Unwanted pursuit, following, or surveillance").icon("👀").active(true).build(),
                Category.builder().name("Other").description("Other safety concerns or grievances").icon("📋").active(true).build()
            );
            categoryRepository.saveAll(newCats);
            
            // Deactivate any old generic categories
            List<String> allowed = List.of("Domestic Violence", "Workplace Harassment", "Cyberbullying", "Public Harassment", "Sexual Assault", "Stalking", "Other");
            categoryRepository.findAll().forEach(cat -> {
                if (!allowed.contains(cat.getName())) {
                    cat.setActive(false);
                    categoryRepository.save(cat);
                }
            });
            
            log.info("✅ {} women's grievance categories seeded & old ones deactivated", newCats.size());
        }
    }
}
