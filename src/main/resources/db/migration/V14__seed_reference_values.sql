INSERT INTO ref.order_types (code, name, description, is_active, created_at, updated_at, created_by, updated_by, is_deleted)
SELECT seed.code, seed.name, seed.description, TRUE, now(), now(), NULL, NULL, FALSE
FROM (
    VALUES
        ('HIRE', 'Прием', 'Приказ о приеме на работу'),
        ('TRANSFER', 'Перевод', 'Приказ о переводе сотрудника'),
        ('SALARY_CHANGE', 'Изменение оклада', 'Приказ об изменении оклада или надбавок'),
        ('LEAVE', 'Отпуск', 'Приказ об отпуске'),
        ('BUSINESS_TRIP', 'Командировка', 'Приказ о командировке'),
        ('DISCIPLINARY_ACTION', 'Дисциплинарное взыскание', 'Приказ о дисциплинарном взыскании'),
        ('REWARD', 'Поощрение', 'Приказ о поощрении'),
        ('DISMISSAL', 'Увольнение', 'Приказ об увольнении')
) AS seed(code, name, description)
WHERE NOT EXISTS (
    SELECT 1 FROM ref.order_types t WHERE lower(t.code) = lower(seed.code) AND t.is_deleted = FALSE
);

INSERT INTO ref.leave_types (
    code, name, description, is_paid, requires_document, available_for_self_service,
    is_active, created_at, updated_at, created_by, updated_by, is_deleted
)
SELECT
    seed.code, seed.name, seed.description, seed.is_paid, seed.requires_document, seed.available_for_self_service,
    TRUE, now(), now(), NULL, NULL, FALSE
FROM (
    VALUES
        ('ANNUAL', 'Ежегодный отпуск', 'Основной оплачиваемый ежегодный отпуск', TRUE, FALSE, TRUE),
        ('ADDITIONAL', 'Дополнительный отпуск', 'Дополнительный оплачиваемый отпуск', TRUE, FALSE, TRUE),
        ('UNPAID', 'Без сохранения заработной платы', 'Отпуск без содержания', FALSE, TRUE, TRUE),
        ('STUDY', 'Учебный отпуск', 'Отпуск для обучения', TRUE, TRUE, TRUE),
        ('MATERNITY', 'Декретный отпуск', 'Отпуск по беременности и родам', TRUE, TRUE, FALSE),
        ('CHILDCARE', 'По уходу за ребенком', 'Отпуск по уходу за ребенком', FALSE, TRUE, FALSE),
        ('SOCIAL', 'Социальный отпуск', 'Социальный отпуск по установленным основаниям', TRUE, TRUE, TRUE)
) AS seed(code, name, description, is_paid, requires_document, available_for_self_service)
WHERE NOT EXISTS (
    SELECT 1 FROM ref.leave_types t WHERE lower(t.code) = lower(seed.code) AND t.is_deleted = FALSE
);

INSERT INTO ref.disciplinary_action_types (
    code, name, description, severity_rank,
    is_active, created_at, updated_at, created_by, updated_by, is_deleted
)
SELECT
    seed.code, seed.name, seed.description, seed.severity_rank,
    TRUE, now(), now(), NULL, NULL, FALSE
FROM (
    VALUES
        ('REMARK', 'Замечание', 'Первичная дисциплинарная мера', 1),
        ('REPRIMAND', 'Выговор', 'Дисциплинарное взыскание средней строгости', 2),
        ('SEVERE_REPRIMAND', 'Строгий выговор', 'Строгая дисциплинарная мера', 3)
) AS seed(code, name, description, severity_rank)
WHERE NOT EXISTS (
    SELECT 1 FROM ref.disciplinary_action_types t WHERE lower(t.code) = lower(seed.code) AND t.is_deleted = FALSE
);

INSERT INTO ref.reward_types (
    code, name, description, is_monetary,
    is_active, created_at, updated_at, created_by, updated_by, is_deleted
)
SELECT
    seed.code, seed.name, seed.description, seed.is_monetary,
    TRUE, now(), now(), NULL, NULL, FALSE
FROM (
    VALUES
        ('THANKS', 'Благодарность', 'Нематериальное поощрение', FALSE),
        ('AWARD', 'Награда', 'Корпоративная награда', FALSE),
        ('CERTIFICATE', 'Грамота', 'Почетная грамота или сертификат', FALSE),
        ('BONUS', 'Премия', 'Денежная премия', TRUE)
    ) AS seed(code, name, description, is_monetary)
WHERE NOT EXISTS (
    SELECT 1 FROM ref.reward_types t WHERE lower(t.code) = lower(seed.code) AND t.is_deleted = FALSE
);

INSERT INTO ref.document_types (
    code, name, description, confidential_by_default,
    is_active, created_at, updated_at, created_by, updated_by, is_deleted
)
SELECT
    seed.code, seed.name, seed.description, seed.confidential_by_default,
    TRUE, now(), now(), NULL, NULL, FALSE
FROM (
    VALUES
        ('PASSPORT', 'Паспорт', 'Паспорт или ID документ', TRUE),
        ('LABOR_CONTRACT', 'Трудовой договор', 'Трудовой договор сотрудника', TRUE),
        ('HIRE_ORDER', 'Приказ о приеме', 'Приказ о приеме на работу', FALSE),
        ('DISMISSAL_ORDER', 'Приказ об увольнении', 'Приказ об увольнении сотрудника', FALSE),
        ('DIPLOMA', 'Диплом', 'Документ об образовании', TRUE),
        ('CERTIFICATION', 'Сертификат', 'Сертификат о квалификации или обучении', FALSE),
        ('SICK_LEAVE_CERT', 'Больничный лист', 'Подтверждение временной нетрудоспособности', TRUE),
        ('BUSINESS_TRIP_REPORT', 'Отчет о командировке', 'Подтверждающие документы по командировке', FALSE),
        ('EXPLANATION_NOTE', 'Объяснительная записка', 'Объяснительная записка сотрудника', TRUE),
        ('OTHER', 'Иной документ', 'Прочие документы сотрудника', FALSE)
) AS seed(code, name, description, confidential_by_default)
WHERE NOT EXISTS (
    SELECT 1 FROM ref.document_types t WHERE lower(t.code) = lower(seed.code) AND t.is_deleted = FALSE
);

INSERT INTO ref.notification_types (
    code, name, description, default_channel,
    is_active, created_at, updated_at, created_by, updated_by, is_deleted
)
SELECT
    seed.code, seed.name, seed.description, seed.default_channel,
    TRUE, now(), now(), NULL, NULL, FALSE
FROM (
    VALUES
        ('TASK_ASSIGNED', 'Назначена задача', 'Системное уведомление о новой задаче', 'IN_APP'),
        ('APPROVAL_REQUEST', 'Запрос на согласование', 'Нужно согласовать документ или заявку', 'IN_APP'),
        ('APPROVAL_RESULT', 'Результат согласования', 'Решение по согласованию', 'IN_APP'),
        ('BIRTHDAY_REMINDER', 'День рождения', 'Напоминание об именинниках', 'IN_APP'),
        ('ORDER_ACKNOWLEDGEMENT', 'Ознакомление с приказом', 'Уведомление об ознакомлении с приказом', 'IN_APP'),
        ('LEAVE_STATUS', 'Статус отпуска', 'Изменение статуса отпуска', 'IN_APP'),
        ('TRIP_STATUS', 'Статус командировки', 'Изменение статуса командировки', 'IN_APP'),
        ('LMS_REMINDER', 'Напоминание об обучении', 'Непройденный обязательный курс', 'IN_APP'),
        ('LIBRARY_OVERDUE', 'Просрочка по библиотеке', 'Напоминание о просроченном возврате книги', 'IN_APP'),
        ('SECURITY_ALERT', 'Сигнал безопасности', 'Сигнал о событии безопасности', 'SYSTEM'),
        ('GENERAL', 'Общее уведомление', 'Произвольное информационное уведомление', 'IN_APP')
    ) AS seed(code, name, description, default_channel)
WHERE NOT EXISTS (
    SELECT 1 FROM ref.notification_types t WHERE lower(t.code) = lower(seed.code) AND t.is_deleted = FALSE
);

INSERT INTO ref.lms_course_types (
    code, name, description, mandatory_by_default,
    is_active, created_at, updated_at, created_by, updated_by, is_deleted
)
SELECT
    seed.code, seed.name, seed.description, seed.mandatory_by_default,
    TRUE, now(), now(), NULL, NULL, FALSE
FROM (
    VALUES
        ('INTRODUCTORY', 'Вводный курс', 'Назначается при приеме на работу', TRUE),
        ('MANDATORY_POSITION', 'Обязательный по должности', 'Обязательный курс по должности', TRUE),
        ('MANDATORY_DEPARTMENT', 'Обязательный по подразделению', 'Обязательный курс по подразделению', TRUE),
        ('COMPLIANCE', 'Комплаенс', 'Обязательный регуляторный или комплаенс курс', TRUE),
        ('OPTIONAL', 'Опциональный', 'Дополнительный курс по развитию', FALSE)
    ) AS seed(code, name, description, mandatory_by_default)
WHERE NOT EXISTS (
    SELECT 1 FROM ref.lms_course_types t WHERE lower(t.code) = lower(seed.code) AND t.is_deleted = FALSE
);
