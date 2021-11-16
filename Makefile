setup:
	gradle wrapper --gradle-version 7.2

clean:
	./gradlew clean

build:
	./gradlew clean build

start:
	./gradlew run

install:
	./gradlew install


generate-migrations:
	./gradlew generateMigrations

lint:
	./gradlew checkstyleMain checkstyleTest

test:
	./gradlew test

report:
	./gradlew jacocoTestReport

check-updates:
	./gradlew dependencyUpdates

.PHONY: build