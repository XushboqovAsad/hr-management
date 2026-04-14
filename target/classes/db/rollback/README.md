# Seed Rollback Strategy

Этот проект использует Flyway только в forward-only режиме через `classpath:db/migration`.
Поэтому rollback для seed-данных хранится отдельно и запускается вручную.

## Порядок

1. Удалить dev dataset:

```sql
\i backend/src/main/resources/db/rollback/dev_dataset__rollback.sql
```

2. Если окружение disposable и reference seed еще не используется бизнес-данными, удалить базовые сиды:

```sql
\i backend/src/main/resources/db/rollback/reference_seed__rollback.sql
```

## Что безопасно откатывать

- `V15` и `V16` рассчитаны на локальную или тестовую среду.
- `V10`–`V14` лучше считать базовыми системными миграциями. В shared окружениях предпочтительнее compensating migration или soft-disable, а не физическое удаление справочников.

## Dev dataset

Все пользователи из `V16__seed_dev_users_dataset.sql` имеют пароль:

`ChangeMe123!`

Логины:

- `dev.superadmin`
- `dev.hr.admin`
- `dev.hr.inspector`
- `dev.manager.sales`
- `dev.payroll`
- `dev.security`
- `dev.employee`
- `dev.auditor`
- `dev.top.management`
