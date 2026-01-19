package com.scutelnic.rutex.service;

import com.scutelnic.rutex.entity.Translation;
import com.scutelnic.rutex.repository.TranslationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class TranslationInitializerService implements CommandLineRunner {
    
    @Autowired
    private TranslationService translationService;
    
    @Autowired
    private TranslationRepository translationRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Initializing translations
        initializeTranslations();
        // Translations initialized
    }
    
    private void initializeTranslations() {
        // Traduceri pentru navbar
        Map<String, String> navbarTranslations = new HashMap<>();
        navbarTranslations.put("nav.brand", "Rutex");
        navbarTranslations.put("nav.home", "Главная");
        navbarTranslations.put("nav.rides", "Поездки");
        navbarTranslations.put("nav.about", "О нас");
        navbarTranslations.put("nav.contact", "Контакты");
        navbarTranslations.put("nav.login", "Вход");
        navbarTranslations.put("nav.logout", "Выход");
        navbarTranslations.put("nav.admin", "Администрирование");
        
        // Traduceri pentru pagina principală
        Map<String, String> indexTranslations = new HashMap<>();
        indexTranslations.put("hero.subtitle", "Подключайтесь к водителям и путешественникам по всей Молдове");
        indexTranslations.put("driver.title", "Вы водитель?");
        indexTranslations.put("driver.create_ride", "Создать поездку");
        indexTranslations.put("driver.subtitle", "Поделитесь своей дорогой и сэкономьте");
        indexTranslations.put("passenger.title", "Вы пассажир?");
        indexTranslations.put("passenger.search_ride", "Найти поездку");
        indexTranslations.put("passenger.subtitle", "Найдите идеальное путешествие для себя");
        indexTranslations.put("packages.title", "Отправляете посылки?");
        indexTranslations.put("packages.search_ride", "Найти транспорт");
        indexTranslations.put("packages.subtitle", "Найдите транспорт для ваших посылок");
        indexTranslations.put("recent_rides.title", "Последние поездки");
        indexTranslations.put("recent_rides.subtitle", "Откройте новые предложения на платформе");
        indexTranslations.put("recent_rides.view_all", "Посмотреть все поездки");
        indexTranslations.put("features.title", "Почему Rutex?");
        indexTranslations.put("features.subtitle", "Откройте преимущества нашей платформы");
        indexTranslations.put("features.eco.title", "Экологичность");
        indexTranslations.put("features.eco.description", "Снижайте углеродный след, делясь автомобилями");
        indexTranslations.put("features.economy.title", "Экономия");
        indexTranslations.put("features.economy.description", "Разделите транспортные расходы с другими путешественниками");
        indexTranslations.put("features.community.title", "Сообщество");
        indexTranslations.put("features.community.description", "Знакомьтесь с новыми людьми и создавайте ценные связи");
        indexTranslations.put("features.rating.title", "Система рейтингов");
        indexTranslations.put("features.rating.description", "Система рейтингов реализована для обеспечения надежных и прозрачных путешествий");
        
        // Traduceri pentru sfaturi de siguranță
        indexTranslations.put("safety_tips.title", "Советы по безопасности для путешественников");
        indexTranslations.put("safety_tips.subtitle", "Нажмите, чтобы увидеть наши советы по безопасности");
        indexTranslations.put("safety_tips.modal_title", "Советы по безопасности для путешественников");
        
        // Traduceri pentru pagina de login
        Map<String, String> loginTranslations = new HashMap<>();
        loginTranslations.put("login.title", "Вход в систему");
        loginTranslations.put("login.welcome", "Добро пожаловать! Войдите в свой аккаунт");
        loginTranslations.put("login.email", "Email");
        loginTranslations.put("login.email_placeholder", "Введите адрес электронной почты");
        loginTranslations.put("login.password", "Пароль");
        loginTranslations.put("login.password_placeholder", "Введите пароль");
        loginTranslations.put("login.remember_me", "Запомнить меня");
        loginTranslations.put("login.submit", "Войти");
        loginTranslations.put("login.no_account", "Нет аккаунта?");
        loginTranslations.put("login.register", "Зарегистрироваться здесь");
        loginTranslations.put("login.forgot_password", "Забыли пароль?");
        
        // Traduceri pentru pagina de înregistrare
        Map<String, String> registerTranslations = new HashMap<>();
        registerTranslations.put("register.title", "Регистрация");
        registerTranslations.put("register.subtitle", "Создайте аккаунт, чтобы начать путешествовать");
        registerTranslations.put("register.first_name", "Имя");
        registerTranslations.put("register.last_name", "Фамилия");
        registerTranslations.put("register.email", "Email");
        registerTranslations.put("register.phone", "Телефон");
        registerTranslations.put("register.profile_image", "Фото профиля");
        registerTranslations.put("register.upload_image", "Загрузить изображение");
        registerTranslations.put("register.remove_image", "Удалить изображение");
        registerTranslations.put("register.password", "Пароль");
        registerTranslations.put("register.confirm_password", "Подтвердите пароль");
        registerTranslations.put("register.terms", "Я согласен с");
        registerTranslations.put("register.newsletter", "Хочу получать уведомления о специальных предложениях");
        registerTranslations.put("register.submit", "Зарегистрироваться");
        registerTranslations.put("register.have_account", "Уже есть аккаунт?");
        registerTranslations.put("register.login", "Войти");
        registerTranslations.put("register.first_name_placeholder", "Введите имя");
        registerTranslations.put("register.last_name_placeholder", "Введите фамилию");
        registerTranslations.put("register.email_placeholder", "Введите адрес электронной почты");
        registerTranslations.put("register.phone_placeholder", "Введите номер телефона");
        registerTranslations.put("register.profile_image_help", "Необязательно - Загрузите изображение для вашего профиля");
        

        registerTranslations.put("register.password_placeholder", "Введите пароль");
        registerTranslations.put("register.confirm_password_placeholder", "Подтвердите пароль");
        registerTranslations.put("register.terms_link", "Условия использования");
        
        // Traduceri pentru pagina de curse
        Map<String, String> ridesTranslations = new HashMap<>();
        ridesTranslations.put("rides.title", "Доступные поездки");
        ridesTranslations.put("rides.subtitle", "Найди идеальную поездку для себя");
        ridesTranslations.put("rides.filter.from", "Откуда");
        ridesTranslations.put("rides.filter.to", "Куда");
        ridesTranslations.put("rides.filter.date", "Дата");
        ridesTranslations.put("rides.select_date", "Выберите дату");
        ridesTranslations.put("rides.filter.packages", "Посылки");
        ridesTranslations.put("rides.filter.any_location", "Любое место");
        ridesTranslations.put("rides.search", "Искать");
        ridesTranslations.put("rides.clear_filters", "Очистить фильтры");
        ridesTranslations.put("rides.found_rides", "Найдено {count} поездок");
        ridesTranslations.put("rides.per_seat", "за место");
        ridesTranslations.put("rides.available_seats", "мест доступно");
        ridesTranslations.put("rides.previous", "Предыдущая");
        ridesTranslations.put("rides.next", "Следующая");
        ridesTranslations.put("rides.no_rides", "Нет доступных поездок");
        ridesTranslations.put("rides.no_rides_message", "Попробуйте изменить фильтры или вернитесь позже.");
        ridesTranslations.put("rides.add_ride", "Добавить поездку");
        ridesTranslations.put("rides.date", "Дата");
        ridesTranslations.put("rides.time", "Время");
        ridesTranslations.put("rides.available_seats_text", "мест");
        ridesTranslations.put("rides.package_only", "Транспортирую только посылки");
        ridesTranslations.put("rides.transport_and_packages", "Транспортирую и посылки");
        ridesTranslations.put("rides.services", "Услуги");
        ridesTranslations.put("rides.views", "Просмотры");
        ridesTranslations.put("rides.manage_ride", "Управление поездкой");
        ridesTranslations.put("rides.edit_ride", "Редактировать поездку");
        ridesTranslations.put("rides.delete_ride", "Удалить поездку");
        ridesTranslations.put("rides.package_transport", "Тип транспорта");
        ridesTranslations.put("rides.per_transport", "за транспорт");
        
        // Traduceri pentru pagina de detalii cursă
        ridesTranslations.put("ride_details.title", "Детали поездки - Rutex");
        ridesTranslations.put("ride_details.route_info", "Информация о маршруте");
        ridesTranslations.put("ride_details.vehicle", "Транспорт");
        ridesTranslations.put("ride_details.driver_info", "Информация о водителе");
        ridesTranslations.put("ride_details.description", "Описание");
        ridesTranslations.put("ride_details.contact_driver", "Связаться с водителем");
        ridesTranslations.put("ride_details.back_to_rides", "Назад к поездкам");
        ridesTranslations.put("ride_details.manage_ride", "Управление поездкой");
        
        // Traduceri pentru pagina de adăugare cursă
        Map<String, String> addRideTranslations = new HashMap<>();
        addRideTranslations.put("add_ride.title", "Создать поездку");
        addRideTranslations.put("add_ride.route_info", "Информация о маршруте");
        addRideTranslations.put("add_ride.from", "Откуда");
        addRideTranslations.put("add_ride.from_placeholder", "Введите место отправления");
        addRideTranslations.put("add_ride.to", "Куда");
        addRideTranslations.put("add_ride.to_placeholder", "Введите место назначения");
        addRideTranslations.put("add_ride.calculate_route", "Рассчитать маршрут");
        addRideTranslations.put("add_ride.clear_route", "Очистить маршрут");
        addRideTranslations.put("add_ride.travel_details", "Детали поездки");
        addRideTranslations.put("add_ride.date", "Дата поездки");
        addRideTranslations.put("add_ride.select_date", "Выберите дату");
        addRideTranslations.put("add_ride.time", "Время отправления");
        addRideTranslations.put("add_ride.select_time", "Выберите время");
        addRideTranslations.put("add_ride.seats", "Количество мест");
        addRideTranslations.put("add_ride.price", "Цена за место (MDL)");
        addRideTranslations.put("add_ride.additional_info", "Дополнительная информация");
        addRideTranslations.put("add_ride.description", "Описание (необязательно)");
        addRideTranslations.put("add_ride.description_placeholder", "Детали о поездке, особые условия и т.д.");
        addRideTranslations.put("add_ride.preview", "Предварительный просмотр");
        addRideTranslations.put("add_ride.submit", "Создать поездку");
        addRideTranslations.put("add_ride.preview_title", "Предварительный просмотр поездки");
        addRideTranslations.put("add_ride.cancel", "Отмена");
        addRideTranslations.put("add_ride.confirm", "Подтвердить добавление");
        
        // Traduceri pentru pagina de profil
        Map<String, String> profileTranslations = new HashMap<>();
        profileTranslations.put("profile.title", "Профиль");
        profileTranslations.put("profile.edit", "Редактировать профиль");
        profileTranslations.put("profile.my_rides", "Мои поездки");
        profileTranslations.put("profile.my_bookings", "Мои бронирования");
        profileTranslations.put("profile.ratings", "Отзывы");
        
        // Traduceri pentru pagina de editare profil
        Map<String, String> editProfileTranslations = new HashMap<>();
        editProfileTranslations.put("edit_profile.title", "Редактировать профиль");
        editProfileTranslations.put("edit_profile.first_name", "Имя");
        editProfileTranslations.put("edit_profile.last_name", "Фамилия");
        editProfileTranslations.put("edit_profile.email", "Email");
        editProfileTranslations.put("edit_profile.phone", "Телефон");
        editProfileTranslations.put("edit_profile.upload_image", "Загрузить изображение");
        editProfileTranslations.put("edit_profile.remove_image", "Удалить изображение");
        editProfileTranslations.put("edit_profile.save", "Сохранить изменения");
        
        // Traduceri pentru pagina de utilizatori (admin)
        Map<String, String> usersTranslations = new HashMap<>();
        usersTranslations.put("users.title", "Управление пользователями");
        usersTranslations.put("users.search", "Поиск пользователей");
        usersTranslations.put("users.name", "Имя");
        usersTranslations.put("users.email", "Email");
        usersTranslations.put("users.role", "Роль");
        usersTranslations.put("users.actions", "Действия");
        usersTranslations.put("users.edit", "Редактировать");
        usersTranslations.put("users.delete", "Удалить");
        
        // Traduceri pentru pagina despre
        Map<String, String> aboutTranslations = new HashMap<>();
        aboutTranslations.put("about.title", "О нас");
        aboutTranslations.put("about.subtitle", "Соединяем людей через общие поездки");
        aboutTranslations.put("about.mission", "Наша миссия");
        aboutTranslations.put("about.mission_text", "Rutex - это инновационная платформа, которая соединяет водителей с путешественниками для облегчения совместных поездок в Молдове. Наша цель - снизить транспортные расходы, продвигать устойчивость и создать сообщество ответственных путешественников.");
        aboutTranslations.put("about.vision", "Наше видение");
        aboutTranslations.put("about.values", "Наши ценности");
        aboutTranslations.put("about.sustainability", "Устойчивость");
        aboutTranslations.put("about.sustainability_text", "Снижаем выбросы CO2, делясь автомобилями и продвигая экологичный транспорт.");
        aboutTranslations.put("about.economy", "Экономия");
        aboutTranslations.put("about.economy_text", "Разделите транспортные расходы и сэкономьте деньги, путешествуя комфортно.");
        aboutTranslations.put("about.community", "Сообщество");
        aboutTranslations.put("about.community_text", "Строим сообщество надежных путешественников и соединяем людей по всей Молдове.");
        aboutTranslations.put("about.rating", "Система рейтингов");
        aboutTranslations.put("about.rating_text", "Система рейтингов реализована для обеспечения надежных и прозрачных путешествий");
        aboutTranslations.put("about.how_it_works", "Как это работает");
        aboutTranslations.put("about.step1_title", "Создайте аккаунт");
        aboutTranslations.put("about.step1_text", "Быстро зарегистрируйтесь и проверьте свой профиль, чтобы начать путешествовать.");
        aboutTranslations.put("about.step2_title", "Ищите или предлагайте поездку");
        aboutTranslations.put("about.step2_text", "Найдите доступные поездки или предложите места в своей машине для ваших путешествий.");
        aboutTranslations.put("about.step3_title", "Бронируйте и путешествуйте");
        aboutTranslations.put("about.step3_text", "Забронируйте место по номеру телефона и наслаждайтесь комфортным и дружелюбным путешествием.");
        aboutTranslations.put("about.why_haidavai", "Почему Rutex?");
        aboutTranslations.put("about.stats.trips", "Реализованные поездки");
        aboutTranslations.put("about.stats.users", "Активные пользователи");
        aboutTranslations.put("about.stats.cities", "Подключенные населенные пункты");
        aboutTranslations.put("about.team", "Наша команда");
        aboutTranslations.put("about.team.alexandru.name", "Александру Попеску");
        aboutTranslations.put("about.team.alexandru.role", "CEO & Основатель");
        aboutTranslations.put("about.team.alexandru.bio", "Увлеченный технологиями и устойчивостью, Александру создал Rutex, чтобы сделать путешествия более доступными и экологичными.");
        aboutTranslations.put("about.team.maria.name", "Мария Ионеску");
        aboutTranslations.put("about.team.maria.role", "CTO");
        aboutTranslations.put("about.team.maria.bio", "Эксперт по разработке программного обеспечения, Мария обеспечивает безупречную работу платформы Rutex для всех пользователей.");
        aboutTranslations.put("about.team.andrei.name", "Андрей Думитреску");
        aboutTranslations.put("about.team.andrei.role", "Руководитель операций");
        aboutTranslations.put("about.team.andrei.bio", "Обеспечивает эффективную работу всех операций и лучший пользовательский опыт.");
        
        // Traduceri pentru pagina de contact
        Map<String, String> contactTranslations = new HashMap<>();
        contactTranslations.put("contact.title", "Контакты");
        contactTranslations.put("contact.get_in_touch", "Свяжитесь с нами");
        contactTranslations.put("contact.send_message", "Отправить нам сообщение");
        contactTranslations.put("contact.first_name", "Имя");
        contactTranslations.put("contact.last_name", "Фамилия");
        contactTranslations.put("contact.email", "Email");
        contactTranslations.put("contact.subject", "Тема");
        contactTranslations.put("contact.select_subject", "Выберите тему");
        contactTranslations.put("contact.subject_general", "Общий вопрос");
        contactTranslations.put("contact.subject_support", "Техническая поддержка");
        contactTranslations.put("contact.subject_partnership", "Партнерство");
        contactTranslations.put("contact.subject_feedback", "Отзыв");
        contactTranslations.put("contact.subject_other", "Другое");
        contactTranslations.put("contact.message", "Сообщение");
        contactTranslations.put("contact.message_placeholder", "Напишите нам ваше сообщение здесь...");
        contactTranslations.put("contact.send", "Отправить сообщение");
        contactTranslations.put("contact.info.email", "Email");
        contactTranslations.put("contact.info.phone", "Телефон");
        contactTranslations.put("contact.info.phone_hours", "Понедельник-Пятница: 9:00-18:00");
        contactTranslations.put("contact.info.address", "Адрес");
        contactTranslations.put("contact.info.street", "Улица Пример, № 123");
        contactTranslations.put("contact.info.city", "Бухарест, Румыния");
        contactTranslations.put("contact.info.schedule", "Расписание");
        contactTranslations.put("contact.info.weekdays", "Понедельник-Пятница: 9:00-18:00");
        contactTranslations.put("contact.info.saturday", "Суббота: 10:00-14:00");
        contactTranslations.put("contact.faq.title", "Часто задаваемые вопросы");
        contactTranslations.put("contact.faq.q1", "Как зарегистрировать поездку?");
        contactTranslations.put("contact.faq.a1", "Для регистрации поездки необходимо войти в свой аккаунт и перейти в раздел 'Добавить поездку'. Там вы сможете заполнить все необходимые детали.");
        contactTranslations.put("contact.faq.q2", "Как работает система платежей?");
        contactTranslations.put("contact.faq.a2", "В настоящее время наш сервис бесплатный. В будущем может быть внедрена система платежей для улучшения пользовательского опыта.");
        contactTranslations.put("contact.faq.q3", "Что происходит, если поездка отменена?");
        contactTranslations.put("contact.faq.a3", "В случае отмены поездки, платформа Rutex не предоставляет возврат средств. Если поездка отменена, пассажир берет на себя ответственность и связанные риски.");
        contactTranslations.put("contact.faq.q4", "Как сообщить о проблеме?");
        contactTranslations.put("contact.faq.a4", "Вы можете сообщить о любой проблеме через контактную форму выше или по email на support@rutex.md. Наша команда ответит в кратчайшие сроки.");
        
        // Traduceri pentru pagina de resetare parolă
        Map<String, String> forgotPasswordTranslations = new HashMap<>();
        forgotPasswordTranslations.put("forgot_password.title", "Забыли пароль");
        forgotPasswordTranslations.put("forgot_password.subtitle", "Введите адрес электронной почты для сброса пароля");
        forgotPasswordTranslations.put("forgot_password.email", "Email");
        forgotPasswordTranslations.put("forgot_password.email_placeholder", "Введите адрес электронной почты");
        forgotPasswordTranslations.put("forgot_password.submit", "Отправить ссылку для сброса");
        forgotPasswordTranslations.put("forgot_password.remember_password", "Помните пароль?");
        forgotPasswordTranslations.put("forgot_password.back_to_login", "Войти здесь");
        
        // Traduceri pentru pagina de resetare parolă (reset-password)
        Map<String, String> resetPasswordTranslations = new HashMap<>();
        resetPasswordTranslations.put("reset_password.title", "Сброс пароля");
        resetPasswordTranslations.put("reset_password.subtitle", "Введите новый пароль для вашего аккаунта");
        resetPasswordTranslations.put("reset_password.new_password", "Новый пароль");
        resetPasswordTranslations.put("reset_password.new_password_placeholder", "Введите новый пароль");
        resetPasswordTranslations.put("reset_password.confirm_password", "Подтвердите пароль");
        resetPasswordTranslations.put("reset_password.confirm_password_placeholder", "Подтвердите новый пароль");
        resetPasswordTranslations.put("reset_password.submit", "Сохранить новый пароль");
        resetPasswordTranslations.put("reset_password.back_to_login", "Назад к");
        resetPasswordTranslations.put("reset_password.login", "входу");
        
        // Traduceri pentru pagina de termeni și condiții
        Map<String, String> termsTranslations = new HashMap<>();
        termsTranslations.put("terms.title", "Условия использования");
        termsTranslations.put("terms.acceptance", "1. Принятие условий");
        termsTranslations.put("terms.acceptance_text", "Получая доступ к платформе Rutex.md и используя её, вы соглашаетесь полностью соблюдать эти условия использования. Если вы не согласны с какой-либо частью этих условий, пожалуйста, не используйте наш сервис.");
        termsTranslations.put("terms.service_description", "2. Описание сервиса");
        termsTranslations.put("terms.service_description_text", "Rutex.md - это онлайн-платформа, которая облегчает связь между водителями и пассажирами для совместных поездок. Платформа не является поставщиком транспортных услуг и не владеет и не управляет транспортными средствами пользователей.");
        termsTranslations.put("terms.user_responsibilities", "3. Обязанности пользователей");
        termsTranslations.put("terms.responsibility_1", "Предоставление точной и полной информации при регистрации.");
        termsTranslations.put("terms.responsibility_2", "Соблюдение действующего законодательства о перевозке пассажиров.");
        termsTranslations.put("terms.responsibility_3", "Поддержание транспортного средства в хорошем рабочем состоянии (для водителей).");
        termsTranslations.put("terms.responsibility_4", "Соблюдение расписания и своевременное сообщение об изменениях.");
        termsTranslations.put("terms.responsibility_5", "Соблюдение правил поведения и политик Rutex.md.");
        termsTranslations.put("terms.payment", "4. Платежи и комиссии");
        termsTranslations.put("terms.payment_text", "Оплата за поездку производится напрямую между водителем и пассажиром. Rutex.md не управляет платежами и не несет ответственности за финансовые споры между пользователями. Платформа может взимать комиссии или административные сборы за использование своих сервисов в соответствии с политикой, отображаемой в приложении.");
        termsTranslations.put("terms.liability", "5. Ограничение ответственности");
        termsTranslations.put("terms.liability_text", "Rutex.md не может быть привлечен к ответственности за несчастные случаи, травмы или ущерб, которые могут возникнуть во время поездок. Пользователи принимают на себя все риски, связанные с поездкой, и несут ответственность за соблюдение применимого законодательства.");
        termsTranslations.put("terms.privacy", "6. Конфиденциальность");
        termsTranslations.put("terms.privacy_text", "Использование персональных данных регулируется нашей Политикой конфиденциальности, которая является неотъемлемой частью этих условий использования.");
        termsTranslations.put("terms.intellectual_property", "7. Интеллектуальная собственность");
        termsTranslations.put("terms.intellectual_property_text", "Все права на платформу, логотипы, дизайн и контент принадлежат Rutex.md. Пользователи не имеют права воспроизводить, распространять или изменять контент без письменного разрешения.");
        termsTranslations.put("terms.modifications", "8. Изменения условий");
        termsTranslations.put("terms.modifications_text", "Rutex.md оставляет за собой право изменять эти условия в любое время. Изменения будут отображаться на платформе и, где это необходимо, сообщаться пользователям по электронной почте или через уведомления в приложении. Продолжение использования сервиса означает принятие обновленных условий.");
        termsTranslations.put("terms.law", "9. Применимое право и юрисдикция");
        termsTranslations.put("terms.law_text", "Эти условия использования регулируются законодательством Республики Молдова. Любой спор будет разрешен компетентными судами Молдовы.");
        termsTranslations.put("terms.contact", "10. Контакты");
        termsTranslations.put("terms.contact_text", "По вопросам или разъяснениям относительно этих условий, свяжитесь с нами:");
        termsTranslations.put("terms.last_updated", "Последнее обновление: 30 августа 2025");
        
        // Additional terms translations for missing sections (Russian)
        termsTranslations.put("terms.acceptance_note", "Примечание: Для определенных функций (например, публикация объявлений) от вас может потребоваться принятие дополнительных/обновленных условий в приложении.");
        termsTranslations.put("terms.no_intermediation", "<strong>Rutex.md не посредничает в платежах</strong> между пользователями, не заключает договоры перевозки от их имени и не гарантирует выполнение поездок.");
        termsTranslations.put("terms.moldova_legal", "<strong>Для Республики Молдова:</strong> публикация или выполнение перевозки пассажиров <em>за плату</em> может требовать лицензий/разрешений. Пользователи несут единоличную ответственность за соблюдение применимых юридических требований.");
        termsTranslations.put("terms.user_content_title", "3.1. Пользовательский контент и отказ от ответственности");
        termsTranslations.put("terms.user_content", "Контент объявлений, сообщений и профилей предоставляется исключительно пользователями. <strong>Rutex.md не проверяет предварительно законность, точность или полноту объявлений</strong> и не гарантирует качество услуг, предлагаемых пользователями.");
        termsTranslations.put("terms.no_responsibility", "Rutex.md <strong>не является стороной</strong> в каком-либо соглашении между водителями и пассажирами и <strong>не несет никакой ответственности</strong> за потери, ущерб, расходы, несчастные случаи, штрафы, задержки или невыполнение поездок.");
        termsTranslations.put("terms.prohibitions_title", "3.2. Запреты");
        termsTranslations.put("terms.prohibitions_1", "Публикация контента, который продвигает <strong>незаконную деятельность</strong> или противоречит добрым нравам.");
        termsTranslations.put("terms.prohibitions_2", "Публикация объявлений о перевозке <strong>за плату</strong> без требуемых законом лицензий/разрешений.");
        termsTranslations.put("terms.prohibitions_3", "Запрос/принятие платежей за поездки через платформу; <strong>Rutex.md не посредничает в платежах</strong>.");
        termsTranslations.put("terms.prohibitions_4", "Публикация обманчивого, непристойного, дискриминационного, насильственного контента или контента, нарушающего авторские права и товарные знаки.");
        termsTranslations.put("terms.prohibitions_5", "Неавторизованный сбор персональных данных других пользователей.");
        termsTranslations.put("terms.prohibitions_6", "Спам, агрессивная реклама или вредоносные ссылки.");
        termsTranslations.put("terms.moderation_title", "3.3. Модерация, приостановка и удаление");
        termsTranslations.put("terms.moderation", "Rutex.md оставляет за собой право без предварительного уведомления <strong>изменять, приостанавливать или удалять</strong> объявления и аккаунты, которые нарушают эти условия, закон или права третьих лиц, а также <strong>ограничивать/прерывать доступ</strong> к сервисам временно или навсегда.");
        termsTranslations.put("terms.abuse_reporting_title", "3.4. Сообщение о злоупотреблениях и сотрудничество с властями");
        termsTranslations.put("terms.abuse_reporting", "Если вы заметили контент, который кажется незаконным или нарушает ваши права, пожалуйста, сообщите об этом на <a href=\"mailto:contact@rutex.md\">contact@rutex.md</a>. Rutex.md проанализирует жалобу и может удалить контент и/или предоставить информацию компетентным властям в соответствии с законом.");
        termsTranslations.put("terms.payment_note", "Любая плата, взимаемая Rutex.md, относится <strong>исключительно</strong> к цифровым услугам (например, продвижение объявления, публикация), а не к фактической перевозке. Rutex.md <strong>не является стороной</strong> в платежах между пользователями.");
        termsTranslations.put("terms.tax_obligations_title", "4.1. Налоговые обязательства и разрешения");
        termsTranslations.put("terms.tax_obligations", "Пользователи несут исключительную ответственность за декларирование и уплату любых налогов, сборов или взносов, а также за получение требуемых законом разрешений и лицензий (например, для перевозки пассажиров <em>за плату</em>).");
        termsTranslations.put("terms.no_warranties", "В максимальной степени, разрешенной законом, Rutex.md не предоставляет гарантий доступности, пригодности или отсутствия ошибок в сервисе и не несет ответственности за потерю прибыли, возможности, данных или другие косвенные убытки.");
        termsTranslations.put("terms.indemnity_title", "5.1. Возмещение ущерба");
        termsTranslations.put("terms.indemnity", "Используя платформу, вы соглашаетесь возместить и защитить Rutex.md, его администраторов и сотрудников от любых претензий, потерь, штрафов, санкций, расходов и затрат (включая гонорары адвокатов), возникающих в результате нарушения этих условий или закона.");
        termsTranslations.put("terms.intellectual_property_contact", "Если вы считаете, что контент нарушает ваши авторские права, вы можете связаться с нами на <a href=\"mailto:contact@rutex.md\">contact@rutex.md</a>.");
        termsTranslations.put("terms.modifications_note", "Мы оставляем за собой право <strong>изменять, приостанавливать или прерывать</strong> частично или полностью сервис без уведомления и без обязательства компенсации.");
        termsTranslations.put("terms.final_provisions_title", "11. Заключительные положения");
        termsTranslations.put("terms.minimum_age", "<strong>Минимальный возраст:</strong> Для использования платформы вам должно быть не менее 18 лет.");
        termsTranslations.put("terms.assignment", "<strong>Уступка:</strong> Вы можете использовать наш сервис, но не можете уступать права/обязанности без нашего письменного согласия. Мы можем уступить сервис другой организации.");
        termsTranslations.put("terms.severability", "<strong>Разделимость:</strong> Если пункт становится недействительным, остальные условия остаются в силе.");
        termsTranslations.put("terms.entire_agreement", "<strong>Полное соглашение:</strong> Эти условия вместе с Политикой конфиденциальности представляют полное соглашение между сторонами относительно использования платформы.");
        termsTranslations.put("terms.contact_email", "Email: <a href=\"mailto:contact@rutex.md\">contact@rutex.md</a>");
        termsTranslations.put("terms.contact_form", "Или заполните форму на странице \"Контакты\".");

        // Traduceri pentru privacy în rusă
        Map<String, String> privacyTranslations = new HashMap<>();
        privacyTranslations.put("privacy.title", "Политика конфиденциальности");
        privacyTranslations.put("privacy.introduction", "1. Введение");
        privacyTranslations.put("privacy.introduction_text", "Rutex.md уважает конфиденциальность своих пользователей и обязуется защищать личную информацию. Эта политика объясняет, как мы собираем, храним, используем и делимся данными пользователей нашей платформы. Получая доступ к нашим услугам или используя их, вы соглашаетесь с практиками, описанными в этой политике.");
        privacyTranslations.put("privacy.definitions", "2. Определения");
        privacyTranslations.put("privacy.definition_1", "<strong>Личные данные:</strong> любая информация, которая позволяет идентифицировать пользователя (например, имя, email, телефон).");
        privacyTranslations.put("privacy.definition_2", "<strong>Сервис:</strong> онлайн-платформа Rutex.md для совместных поездок.");
        privacyTranslations.put("privacy.definition_3", "<strong>Пользователь:</strong> любое лицо, которое получает доступ к сервису Rutex.md или использует его.");
        privacyTranslations.put("privacy.collected_data", "3. Собираемая информация");
        privacyTranslations.put("privacy.personal_info", "3.1 Личная информация");
        privacyTranslations.put("privacy.personal_1", "Имя и фамилия");
        privacyTranslations.put("privacy.personal_2", "Адрес электронной почты");
        privacyTranslations.put("privacy.personal_3", "Номер телефона");
        privacyTranslations.put("privacy.personal_4", "Информация, связанная с аккаунтом и аутентификацией");
        privacyTranslations.put("privacy.usage_data", "3.2 Информация об использовании");
        privacyTranslations.put("privacy.usage_1", "История поездок и бронирований");
        privacyTranslations.put("privacy.usage_2", "Предпочтения в поездках");
        privacyTranslations.put("privacy.usage_3", "Тип устройства и браузера");
        privacyTranslations.put("privacy.usage_4", "IP-адрес, приблизительное местоположение и данные геолокации (с согласия)");
        privacyTranslations.put("privacy.usage_5", "Файлы cookie и аналогичные технологии");
        privacyTranslations.put("privacy.how_we_use", "4. Как мы используем информацию");
        privacyTranslations.put("privacy.how_we_use_text", "Собранная информация используется для:");
        privacyTranslations.put("privacy.use_1", "Предоставления услуг платформы и функций совместных поездок.");
        privacyTranslations.put("privacy.use_2", "Эффективного соединения водителей и пассажиров.");
        privacyTranslations.put("privacy.use_3", "Улучшения пользовательского опыта и персонализации интерфейса и рекомендаций.");
        privacyTranslations.put("privacy.use_4", "Отправки уведомлений и сообщений, связанных с аккаунтом, бронированиями и услугами.");
        privacyTranslations.put("privacy.use_5", "Мониторинга безопасности платформы и предотвращения мошенничества или злоупотреблений.");
        privacyTranslations.put("privacy.use_6", "Соблюдения юридических обязательств и требований властей.");
        privacyTranslations.put("privacy.sharing", "5. Обмен информацией");
        privacyTranslations.put("privacy.sharing_text", "Rutex.md не продает и не сдает в аренду личные данные пользователей. Информация может быть передана в следующих ситуациях:");
        privacyTranslations.put("privacy.sharing_1", "С явного согласия пользователя.");
        privacyTranslations.put("privacy.sharing_2", "Для соблюдения применимого законодательства или требований властей.");
        privacyTranslations.put("privacy.sharing_3", "С партнерами или поставщиками услуг, которые помогают в работе платформы (например, процессоры платежей, сервисы электронной почты, ИТ-сервисы).");
        privacyTranslations.put("privacy.sharing_4", "Для защиты прав, безопасности и целостности пользователей или платформы.");
        privacyTranslations.put("privacy.security", "6. Безопасность данных");
        privacyTranslations.put("privacy.security_text", "Мы внедряем передовые технические и организационные меры для защиты данных, включая:");
        privacyTranslations.put("privacy.security_1", "Шифрование коммуникаций и хранимых данных.");
        privacyTranslations.put("privacy.security_2", "Брандмауэры и системы обнаружения вторжений.");
        privacyTranslations.put("privacy.security_3", "Строгий контроль доступа сотрудников и партнеров.");
        privacyTranslations.put("privacy.security_4", "Периодическое резервное копирование и планы обеспечения непрерывности сервиса.");
        privacyTranslations.put("privacy.cookies", "7. Файлы cookie и аналогичные технологии");
        privacyTranslations.put("privacy.cookies_text", "Платформа использует файлы cookie для:");
        privacyTranslations.put("privacy.cookies_1", "Улучшения пользовательского опыта.");
        privacyTranslations.put("privacy.cookies_2", "Анализа трафика и производительности сайта.");
        privacyTranslations.put("privacy.cookies_3", "Персонализации контента и рекомендаций.");
        privacyTranslations.put("privacy.cookies_note", "Пользователи могут управлять файлами cookie через настройки браузера. Некоторые функции могут быть затронуты, если файлы cookie отключены.");
        privacyTranslations.put("privacy.your_rights", "8. Права пользователей");
        privacyTranslations.put("privacy.your_rights_text", "В соответствии с применимым законодательством пользователи имеют право:");
        privacyTranslations.put("privacy.rights_1", "Получать доступ к хранимым личным данным.");
        privacyTranslations.put("privacy.rights_2", "Исправлять или обновлять неточную информацию.");
        privacyTranslations.put("privacy.rights_3", "Удалить аккаунт и связанные данные.");
        privacyTranslations.put("privacy.rights_4", "Отозвать согласие, данное на обработку данных.");
        privacyTranslations.put("privacy.rights_5", "Возражать против обработки данных в определенных целях.");
        privacyTranslations.put("privacy.rights_6", "Запрашивать ограничение обработки или переносимость данных.");
        privacyTranslations.put("privacy.law", "9. Применимое право и юрисдикция");
        privacyTranslations.put("privacy.law_text", "Политика конфиденциальности и использование платформы Rutex.md регулируются законодательством Республики Молдова. Любой спор будет разрешен в компетентных судах Республики Молдова.");
        privacyTranslations.put("privacy.modifications", "10. Изменения политики");
        privacyTranslations.put("privacy.modifications_text", "Rutex.md оставляет за собой право изменять политику конфиденциальности. Любые изменения будут отображаться на этой странице вместе с датой последнего обновления. Пользователь несет ответственность за периодическую проверку этой страницы.");
        privacyTranslations.put("privacy.contact", "11. Контакты");
        privacyTranslations.put("privacy.contact_text", "По вопросам, касающимся этой политики, или для осуществления ваших прав, свяжитесь с нами:");
        privacyTranslations.put("privacy.last_updated", "Последнее обновление: 20 августа 2025");

        // Traduceri pentru română - aceleași chei dar cu text român
        Map<String, String> termsTranslationsRo = new HashMap<>();
        Map<String, String> navbarTranslationsRo = new HashMap<>();
        Map<String, String> footerTranslationsRo = new HashMap<>();
        
        // Traduceri pentru navbar în română
        navbarTranslationsRo.put("nav.brand", "Rutex");
        navbarTranslationsRo.put("nav.home", "Acasă");
        navbarTranslationsRo.put("nav.rides", "Călătorii");
        navbarTranslationsRo.put("nav.about", "Despre");
        navbarTranslationsRo.put("nav.contact", "Contact");
        navbarTranslationsRo.put("nav.login", "Conectare");
        navbarTranslationsRo.put("nav.logout", "Deconectare");
        navbarTranslationsRo.put("nav.admin", "Administrare");
        
        // Traduceri pentru footer în română
        footerTranslationsRo.put("footer.tagline", "Conectăm oamenii prin călătorii împărtășite");
        footerTranslationsRo.put("footer.useful_links", "Link-uri utile");
        footerTranslationsRo.put("footer.about_us", "Despre noi");
        footerTranslationsRo.put("footer.terms", "Termeni și condiții");
        footerTranslationsRo.put("footer.privacy", "Politica de confidențialitate");
        footerTranslationsRo.put("footer.contact", "Contact");
        footerTranslationsRo.put("footer.copyright", "© 2026 Rutex. Toate drepturile rezervate.");
        
        // Traduceri pentru terms în română
        termsTranslationsRo.put("terms.payment", "4. Plata și comisioane");
        termsTranslationsRo.put("terms.payment_text", "Plata pentru călătorie se face direct între șofer și pasager. Rutex.md nu gestionează plățile și nu este responsabil pentru disputele financiare dintre utilizatori. Platforma poate percepe comisioane sau taxe administrative pentru utilizarea serviciilor sale, conform politicii afișate în aplicație.");
        termsTranslationsRo.put("terms.liability", "5. Limitarea responsabilității");
        termsTranslationsRo.put("terms.liability_text", "Rutex.md nu poate fi tras la răspundere pentru accidente, vătămări sau daune care pot apărea în timpul călătoriilor. Utilizatorii își asumă toate riscurile asociate cu călătoria și sunt responsabili pentru respectarea legislației aplicabile.");
        termsTranslationsRo.put("terms.privacy", "6. Confidențialitate");
        termsTranslationsRo.put("terms.privacy_text", "Utilizarea datelor personale este reglementată de Politica noastră de confidențialitate, care face parte integrantă din acești termeni și condiții.");
        termsTranslationsRo.put("terms.intellectual_property", "7. Proprietate intelectuală");
        termsTranslationsRo.put("terms.intellectual_property_text", "Toate drepturile asupra platformei, logo-urilor, designului și conținutului sunt deținute de Rutex.md. Utilizatorii nu au voie să reproducă, distribuie sau modifice conținutul fără permisiune scrisă.");
        termsTranslationsRo.put("terms.modifications", "8. Modificări ale termenilor");
        termsTranslationsRo.put("terms.modifications_text", "Rutex.md își rezervă dreptul de a modifica acești termeni în orice moment. Modificările vor fi afișate pe platformă și, acolo unde este necesar, comunicate utilizatorilor prin email sau notificări în aplicație. Continuarea utilizării serviciului implică acceptarea termenilor actualizați.");
        termsTranslationsRo.put("terms.law", "9. Legea aplicabilă și jurisdicția");
        termsTranslationsRo.put("terms.law_text", "Acești termeni și condiții sunt guvernați de legislația Republicii Moldova. Orice dispută va fi soluționată de instanțele competente din Moldova.");
        termsTranslationsRo.put("terms.contact", "10. Contact");
        termsTranslationsRo.put("terms.contact_text", "Pentru întrebări sau clarificări privind acești termeni, contactați-ne la:");
        termsTranslationsRo.put("terms.last_updated", "Ultima actualizare: 30 august 2025");
        termsTranslationsRo.put("terms.acceptance_note", "Notă: Pentru anumite funcții (ex. publicarea anunțurilor), vi se poate cere să acceptați termeni suplimentari/actualizați în aplicație.");
        termsTranslationsRo.put("terms.no_intermediation", "<strong>Rutex.md nu intermediază plăți</strong> între utilizatori, nu încheie contracte de transport în numele acestora și nu garantează efectuarea călătoriilor.");
        termsTranslationsRo.put("terms.moldova_legal", "<strong>Pentru Republica Moldova:</strong> publicarea sau efectuarea de transport de persoane <em>contra cost</em> poate necesita licențe/autorizații. Utilizatorii sunt singurii responsabili să respecte cerințele legale aplicabile.");
        termsTranslationsRo.put("terms.user_content_title", "3.1. Conținut generat de utilizatori și declinare de responsabilitate");
        termsTranslationsRo.put("terms.user_content", "Conținutul anunțurilor, mesajelor și profilurilor este furnizat exclusiv de utilizatori. <strong>Rutex.md nu verifică prealabil legalitatea, acuratețea sau completitudinea anunțurilor</strong> și nu garantează calitatea serviciilor oferite de utilizatori.");
        termsTranslationsRo.put("terms.no_responsibility", "Rutex.md <strong>nu este parte</strong> la niciun acord dintre șoferi și pasageri și <strong>nu își asumă nicio răspundere</strong> pentru pierderi, daune, cheltuieli, accidente, amenzi, întârzieri sau neefectuarea călătoriilor.");
        termsTranslationsRo.put("terms.prohibitions_title", "3.2. Interdicții");
        termsTranslationsRo.put("terms.prohibitions_1", "Publicarea de conținut care promovează activități <strong>ilegale</strong> sau contrare bunelor moravuri.");
        termsTranslationsRo.put("terms.prohibitions_2", "Publicarea de anunțuri pentru transport <strong>contra cost</strong> fără licențe/autorizații impuse de lege.");
        termsTranslationsRo.put("terms.prohibitions_3", "Solicitarea/acceptarea de plăți pentru curse prin platformă; <strong>Rutex.md nu intermediază plăți</strong>.");
        termsTranslationsRo.put("terms.prohibitions_4", "Publicarea de conținut înșelător, obscen, discriminatoriu, violent, sau care încalcă drepturile de autor și marca.");
        termsTranslationsRo.put("terms.prohibitions_5", "Colectarea neautorizată de date personale ale altor utilizatori.");
        termsTranslationsRo.put("terms.prohibitions_6", "Spam, publicitate agresivă sau linkuri rău-intenționate.");
        termsTranslationsRo.put("terms.moderation_title", "3.3. Moderare, suspendare și ștergere");
        termsTranslationsRo.put("terms.moderation", "Rutex.md își rezervă dreptul, fără notificare prealabilă, să <strong>modifice, suspende sau șteargă</strong> anunțuri și conturi care încalcă acești termeni, legea sau drepturile terților, precum și să <strong>restricționeze/întrerupă accesul</strong> la servicii, temporar sau definitiv.");
        termsTranslationsRo.put("terms.abuse_reporting_title", "3.4. Raportare abuz și cooperare cu autoritățile");
        termsTranslationsRo.put("terms.abuse_reporting", "Dacă observați conținut care pare ilegal sau vă încalcă drepturile, vă rugăm să îl raportați la <a href=\"mailto:contact@rutex.md\">contact@rutex.md</a>. Rutex.md va analiza sesizarea și poate elimina conținutul și/sau furniza informații autorităților competente conform legii.");
        termsTranslationsRo.put("terms.payment_note", "Orice tarif perceput de Rutex.md se referă <strong>exclusiv</strong> la servicii digitale (ex. promovare anunț, publicare), nu la transportul efectiv. Rutex.md <strong>nu este parte</strong> la plățile dintre utilizatori.");
        termsTranslationsRo.put("terms.tax_obligations_title", "4.1. Obligații fiscale și autorizații");
        termsTranslationsRo.put("terms.tax_obligations", "Utilizatorii sunt exclusiv responsabili pentru declararea și plata oricăror impozite, taxe sau contribuții, precum și pentru obținerea autorizațiilor și licențelor cerute de lege (de ex., pentru transport de persoane <em>contra cost</em>).");
        termsTranslationsRo.put("terms.no_warranties", "În măsura permisă de lege, Rutex.md nu oferă garanții de disponibilitate, adecvare sau lipsă de erori a serviciului și nu răspunde pentru pierderi de profit, oportunitate, date sau alte daune indirecte.");
        termsTranslationsRo.put("terms.indemnity_title", "5.1. Despăgubiri");
        termsTranslationsRo.put("terms.indemnity", "Prin utilizarea platformei, sunteți de acord să despăgubiți și să protejați Rutex.md, administratorii și colaboratorii săi împotriva oricăror pretenții, pierderi, amenzi, sancțiuni, costuri și cheltuieli (inclusiv onorarii de avocat) rezultate din încălcarea acestor termeni sau a legii.");
        termsTranslationsRo.put("terms.intellectual_property_contact", "Dacă considerați că un conținut vă încalcă drepturile de autor, ne puteți contacta la <a href=\"mailto:contact@rutex.md\">contact@rutex.md</a>.");
        termsTranslationsRo.put("terms.modifications_note", "Ne rezervăm dreptul de a <strong>modifica, suspenda sau întrerupe</strong> parțial sau total serviciul, fără notificare, și fără a datora compensații.");
        termsTranslationsRo.put("terms.final_provisions_title", "11. Dispoziții finale");
        termsTranslationsRo.put("terms.minimum_age", "<strong>Vârsta minimă:</strong> Pentru a utiliza platforma trebuie să aveți cel puțin 18 ani.");
        termsTranslationsRo.put("terms.assignment", "<strong>Cesionare:</strong> Ne puteți utiliza serviciul, dar nu puteți cesiona drepturile/obligațiile fără acordul nostru scris. Noi putem cesiona serviciul către o altă entitate.");
        termsTranslationsRo.put("terms.severability", "<strong>Severabilitate:</strong> Dacă o clauză devine nevalidă, restul termenilor rămân valabili.");
        termsTranslationsRo.put("terms.entire_agreement", "<strong>Întregul acord:</strong> Acești termeni, împreună cu Politica de confidențialitate, reprezintă întregul acord dintre părți privind utilizarea platformei.");
        termsTranslationsRo.put("terms.contact_email", "Email: <a href=\"mailto:contact@rutex.md\">contact@rutex.md</a>");
        termsTranslationsRo.put("terms.contact_form", "Sau îndepliniți formularul de la pagina \"Contact\".");

        // Traduceri pentru privacy în română
        Map<String, String> privacyTranslationsRo = new HashMap<>();
        privacyTranslationsRo.put("privacy.title", "Politica de confidențialitate");
        privacyTranslationsRo.put("privacy.introduction", "1. Introducere");
        privacyTranslationsRo.put("privacy.introduction_text", "Rutex.md respectă confidențialitatea utilizatorilor săi și se angajează să protejeze informațiile personale. Această politică explică modul în care colectăm, stocăm, utilizăm și partajăm datele utilizatorilor platformei noastre. Prin accesarea sau utilizarea serviciilor noastre, sunteți de acord cu practicile descrise în această politică.");
        privacyTranslationsRo.put("privacy.definitions", "2. Definiții");
        privacyTranslationsRo.put("privacy.definition_1", "<strong>Date personale:</strong> orice informație care permite identificarea unui utilizator (ex: nume, email, telefon).");
        privacyTranslationsRo.put("privacy.definition_2", "<strong>Serviciu:</strong> platforma online Rutex.md pentru ride-sharing.");
        privacyTranslationsRo.put("privacy.definition_3", "<strong>Utilizator:</strong> orice persoană care accesează sau folosește serviciul Rutex.md.");
        privacyTranslationsRo.put("privacy.collected_data", "3. Informațiile colectate");
        privacyTranslationsRo.put("privacy.personal_info", "3.1 Informații personale");
        privacyTranslationsRo.put("privacy.personal_1", "Numele și prenumele");
        privacyTranslationsRo.put("privacy.personal_2", "Adresa de email");
        privacyTranslationsRo.put("privacy.personal_3", "Numărul de telefon");
        privacyTranslationsRo.put("privacy.personal_4", "Informații legate de cont și autentificare");
        privacyTranslationsRo.put("privacy.usage_data", "3.2 Informații de utilizare");
        privacyTranslationsRo.put("privacy.usage_1", "Istoricul călătoriilor și rezervărilor");
        privacyTranslationsRo.put("privacy.usage_2", "Preferințele de călătorie");
        privacyTranslationsRo.put("privacy.usage_3", "Tipul de dispozitiv și browser utilizat");
        privacyTranslationsRo.put("privacy.usage_4", "Adresă IP, locație aproximativă și date de geolocație (cu consimțământ)");
        privacyTranslationsRo.put("privacy.usage_5", "Cookie-uri și tehnologii similare");
        privacyTranslationsRo.put("privacy.how_we_use", "4. Cum folosim informațiile");
        privacyTranslationsRo.put("privacy.how_we_use_text", "Informațiile colectate sunt utilizate pentru a:");
        privacyTranslationsRo.put("privacy.use_1", "Oferi serviciile platformei și pentru funcționalitățile de ride-sharing.");
        privacyTranslationsRo.put("privacy.use_2", "Permite conectarea între șoferi și pasageri în mod eficient.");
        privacyTranslationsRo.put("privacy.use_3", "Îmbunătăți experiența utilizatorului și personaliza interfața și recomandările.");
        privacyTranslationsRo.put("privacy.use_4", "Trimite notificări și comunicări legate de cont, rezervări și servicii.");
        privacyTranslationsRo.put("privacy.use_5", "Monitoriza securitatea platformei și preveni fraudele sau abuzurile.");
        privacyTranslationsRo.put("privacy.use_6", "Respecta obligațiile legale și cerințele autorităților.");
        privacyTranslationsRo.put("privacy.sharing", "5. Partajarea informațiilor");
        privacyTranslationsRo.put("privacy.sharing_text", "Rutex.md nu vinde sau închiriază datele personale ale utilizatorilor. Informațiile pot fi partajate în următoarele situații:");
        privacyTranslationsRo.put("privacy.sharing_1", "Cu consimțământul explicit al utilizatorului.");
        privacyTranslationsRo.put("privacy.sharing_2", "Pentru a respecta legislația aplicabilă sau cerințele autorităților.");
        privacyTranslationsRo.put("privacy.sharing_3", "Cu parteneri sau furnizori de servicii care ajută la operarea platformei (ex: procesatori de plăți, servicii de email, servicii IT).");
        privacyTranslationsRo.put("privacy.sharing_4", "Pentru protejarea drepturilor, siguranței și integrității utilizatorilor sau platformei.");
        privacyTranslationsRo.put("privacy.security", "6. Securitatea datelor");
        privacyTranslationsRo.put("privacy.security_text", "Implementăm măsuri tehnice și organizatorice avansate pentru protecția datelor, inclusiv:");
        privacyTranslationsRo.put("privacy.security_1", "Criptarea comunicațiilor și a datelor stocate.");
        privacyTranslationsRo.put("privacy.security_2", "Firewall-uri și sisteme de detectare a intruziunilor.");
        privacyTranslationsRo.put("privacy.security_3", "Control strict al accesului angajaților și al partenerilor.");
        privacyTranslationsRo.put("privacy.security_4", "Backup periodic și planuri de continuitate a serviciului.");
        privacyTranslationsRo.put("privacy.cookies", "7. Cookie-uri și tehnologii similare");
        privacyTranslationsRo.put("privacy.cookies_text", "Platforma utilizează cookie-uri pentru:");
        privacyTranslationsRo.put("privacy.cookies_1", "Îmbunătățirea experienței utilizatorilor.");
        privacyTranslationsRo.put("privacy.cookies_2", "Analiza traficului și performanței site-ului.");
        privacyTranslationsRo.put("privacy.cookies_3", "Personalizarea conținutului și recomandărilor.");
        privacyTranslationsRo.put("privacy.cookies_note", "Utilizatorii pot gestiona cookie-urile prin setările browserului. Unele funcționalități pot fi afectate dacă cookie-urile sunt dezactivate.");
        privacyTranslationsRo.put("privacy.your_rights", "8. Drepturile utilizatorilor");
        privacyTranslationsRo.put("privacy.your_rights_text", "Conform legislației aplicabile, utilizatorii au dreptul de a:");
        privacyTranslationsRo.put("privacy.rights_1", "Accesa datele personale stocate.");
        privacyTranslationsRo.put("privacy.rights_2", "Corecta sau actualiza informațiile inexacte.");
        privacyTranslationsRo.put("privacy.rights_3", "Șterge contul și datele asociate.");
        privacyTranslationsRo.put("privacy.rights_4", "Retrage consimțământul acordat pentru procesarea datelor.");
        privacyTranslationsRo.put("privacy.rights_5", "Se opune prelucrării datelor în anumite scopuri.");
        privacyTranslationsRo.put("privacy.rights_6", "Solicita limitarea prelucrării sau portabilitatea datelor.");
        privacyTranslationsRo.put("privacy.law", "9. Legea aplicabilă și jurisdicția");
        privacyTranslationsRo.put("privacy.law_text", "Politica de confidențialitate și utilizarea platformei Rutex.md sunt guvernate de legislația Republicii Moldova. Orice dispută va fi soluționată în instanțele competente din Republica Moldova.");
        privacyTranslationsRo.put("privacy.modifications", "10. Modificări ale politicii");
        privacyTranslationsRo.put("privacy.modifications_text", "Rutex.md își rezervă dreptul de a modifica politica de confidențialitate. Orice modificare va fi afișată pe această pagină, împreună cu data ultimei actualizări. Este responsabilitatea utilizatorului să verifice periodic această pagină.");
        privacyTranslationsRo.put("privacy.contact", "11. Contact");
        privacyTranslationsRo.put("privacy.contact_text", "Pentru întrebări privind această politică sau pentru a vă exercita drepturile, contactați-ne la:");
        privacyTranslationsRo.put("privacy.last_updated", "Ultima actualizare: 20 august 2026");

        // Traduceri pentru footer
        Map<String, String> footerTranslations = new HashMap<>();
        footerTranslations.put("footer.tagline", "Соединяем людей через общие поездки");
        footerTranslations.put("footer.useful_links", "Полезные ссылки");
        footerTranslations.put("footer.about_us", "О нас");
        footerTranslations.put("footer.terms", "Условия использования");
        footerTranslations.put("footer.privacy", "Политика конфиденциальности");
        footerTranslations.put("footer.contact", "Контакты");
        footerTranslations.put("footer.copyright", "© 2026 Rutex. Все права защищены.");
        
        // Salvăm toate traducerile
        saveTranslations(navbarTranslations, "navbar", "ro", "ru");
        saveTranslations(indexTranslations, "index", "ro", "ru");
        saveTranslations(loginTranslations, "login", "ro", "ru");
        saveTranslations(registerTranslations, "register", "ro", "ru");
        saveTranslations(ridesTranslations, "rides", "ro", "ru");
        saveTranslations(addRideTranslations, "add-ride", "ro", "ru");
        saveTranslations(profileTranslations, "profile", "ro", "ru");
        saveTranslations(editProfileTranslations, "edit-profile", "ro", "ru");
        saveTranslations(usersTranslations, "users", "ro", "ru");
        saveTranslations(aboutTranslations, "about", "ro", "ru");
        saveTranslations(contactTranslations, "contact", "ro", "ru");
        saveTranslations(forgotPasswordTranslations, "forgot-password", "ro", "ru");
        saveTranslations(resetPasswordTranslations, "reset-password", "ro", "ru");
        // Forțăm actualizarea traducerilor pentru terms
        
        saveTranslations(termsTranslations, "terms", "ro", "ru");
        saveTranslations(termsTranslationsRo, "terms", "ro", "ro");
        saveTranslations(privacyTranslations, "privacy", "ro", "ru");
        saveTranslations(privacyTranslationsRo, "privacy", "ro", "ro");
        saveTranslations(navbarTranslationsRo, "navbar", "ro", "ro");
        saveTranslations(footerTranslationsRo, "footer", "ro", "ro");
        saveTranslations(footerTranslations, "footer", "ro", "ru");
        
        int totalTranslations = navbarTranslations.size() + indexTranslations.size() + 
                              loginTranslations.size() + registerTranslations.size() + 
                              ridesTranslations.size() + addRideTranslations.size() + 
                              profileTranslations.size() + editProfileTranslations.size() + 
                              usersTranslations.size() + aboutTranslations.size() + 
                              contactTranslations.size() + forgotPasswordTranslations.size() + 
                              resetPasswordTranslations.size() + footerTranslations.size() +
                              privacyTranslations.size();
        
        // Initialized translations for all pages
        
        // Actualizăm forțat traducerea pentru prețul din add-ride
        forceUpdateTranslation("add-ride", "add_ride.price", "Цена за место (MDL)", "ro", "ru");
        
        // Actualizăm forțat traducerile pentru navbar
        forceUpdateTranslation("navbar", "nav.login", "Вход", "ro", "ru");
        forceUpdateTranslation("navbar", "nav.logout", "Выход", "ro", "ru");
        forceUpdateTranslation("navbar", "nav.home", "Главная", "ro", "ru");
        forceUpdateTranslation("navbar", "nav.rides", "Поездки", "ro", "ru");
        forceUpdateTranslation("navbar", "nav.about", "О нас", "ro", "ru");
        forceUpdateTranslation("navbar", "nav.contact", "Контакты", "ro", "ru");
        forceUpdateTranslation("navbar", "nav.admin", "Администрирование", "ro", "ru");
        
        // Actualizăm forțat traducerile pentru FAQ-ul din contact
        forceUpdateTranslation("contact", "contact.faq.a2", "В настоящее время наш сервис бесплатный. В будущем может быть внедрена система платежей для улучшения пользовательского опыта.", "ro", "ru");
        forceUpdateTranslation("contact", "contact.faq.a3", "В случае отмены поездки, платформа Rutex не предоставляет возврат средств. Если поездка отменена, пассажир берет на себя ответственность и связанные риски.", "ro", "ru");
        
        // Actualizăm forțat copyright-ul pentru footer în rusă
        forceUpdateTranslation("footer", "footer.copyright", "© 2026 Rutex. Все права защищены.", "ro", "ru");
        
        // Actualizăm forțat traducerile pentru pagina despre
        forceUpdateTranslation("about", "about.stats.cities", "Подключенные населенные пункты", "ro", "ru");
        forceUpdateTranslation("about", "about.rating", "Система рейтингов", "ro", "ru");
        forceUpdateTranslation("about", "about.rating_text", "Система рейтингов реализована для обеспечения надежных и прозрачных путешествий", "ro", "ru");
        forceUpdateTranslation("about", "about.step3_text", "Забронируйте место по номеру телефона и наслаждайтесь комфортным и дружелюбным путешествием.", "ro", "ru");
        
        // Actualizăm forțat traducerile pentru pagina index (features)
        forceUpdateTranslation("index", "features.rating.title", "Система рейтингов", "ro", "ru");
        forceUpdateTranslation("index", "features.rating.description", "Система рейтингов реализована для обеспечения надежных и прозрачных путешествий", "ro", "ru");
        
        // Actualizăm forțat traducerea pentru comunitate în rusă
        forceUpdateTranslation("about", "about.community_text", "Строим сообщество надежных путешественников и соединяем людей по всей Молдове.", "ro", "ru");
        
        // Actualizăm forțat traducerile pentru sfaturi de siguranță
        forceUpdateTranslation("index", "safety_tips.title", "Советы по безопасности для путешественников", "ro", "ru");
        forceUpdateTranslation("index", "safety_tips.subtitle", "Нажмите, чтобы увидеть наши советы по безопасности", "ro", "ru");
        forceUpdateTranslation("index", "safety_tips.modal_title", "Советы по безопасности для путешественников", "ro", "ru");
        
        // Actualizăm forțat traducerea pentru services
        forceUpdateTranslation("rides", "rides.services", "Услуги", "ro", "ru");
        
        // Actualizăm forțat traducerea pentru package_only
        forceUpdateTranslation("rides", "rides.package_only", "Транспортирую только посылки", "ro", "ru");
    }
    
    // Metodă pentru actualizarea forțată a unei traduceri specifice
    private void forceUpdateTranslation(String pageName, String sourceText, String targetText, String sourceLang, String targetLang) {
        String translationKey = pageName + "_" + sourceText.hashCode();
        
        // Căutăm traducerea după cheie și limbi
        Optional<Translation> existingTranslationOpt = translationRepository.findByKeyAndLanguages(translationKey, sourceLang, targetLang);
        
        if (existingTranslationOpt.isPresent()) {
            Translation existingTranslation = existingTranslationOpt.get();
            existingTranslation.setTranslatedText(targetText);
            translationRepository.save(existingTranslation);
            // Force updated translation
        } else {
            // Dacă nu există, o creăm
            Translation translation = new Translation(
                translationKey, sourceText, targetText, sourceLang, targetLang, pageName
            );
            translationRepository.save(translation);
            // Created new translation
        }
    }
    
    private void saveTranslations(Map<String, String> translations, String pageName, String sourceLang, String targetLang) {
        for (Map.Entry<String, String> entry : translations.entrySet()) {
            String sourceText = entry.getKey();
            String translatedText = entry.getValue();
            
            try {
                String translationKey = pageName + "_" + sourceText.hashCode();
                
                // Căutăm traducerea existentă
                Optional<Translation> existingTranslationOpt = translationRepository.findByKeyAndLanguages(translationKey, sourceLang, targetLang);
                
                if (existingTranslationOpt.isPresent()) {
                    // Actualizăm traducerea existentă
                    Translation existingTranslation = existingTranslationOpt.get();
                    existingTranslation.setTranslatedText(translatedText);
                    translationRepository.save(existingTranslation);
                } else {
                    // Creăm o traducere nouă
                    Translation translation = new Translation(
                        translationKey, sourceText, translatedText, sourceLang, targetLang, pageName
                    );
                    translationRepository.save(translation);
                }
            } catch (Exception e) {
                System.err.println("Error saving translation for " + sourceText + ": " + e.getMessage());
            }
        }
    }
}
