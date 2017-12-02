Работа с репозиторием
====

[Форкните проект](https://help.github.com/articles/fork-a-repo/), склонируйте и добавьте `upstream`:
```
$ git clone https://<username>@github.com/techno-aads/java-advanced-2017.git
  Cloning into 'java-advanced-2017'...
  remote: Counting objects: 467, done.
  remote: Total 467 (delta 0), reused 0 (delta 0), pack-reused 467
  Receiving objects: 100% (467/467), 1.30 MiB | 970.00 KiB/s, done.
  Resolving deltas: 100% (131/131), done.
$ git remote add upstream https://github.com/techno-aads/java-advanced-2017.git
$ git fetch upstream
```
Затем перейдите в бранч со своей [фамилией](https://github.com/techno-aads/java-advanced-2017/branches/all), пишите в нём код и отправляйте как Pull Request в бранч solve

Описание тестов к курсу «Технологии Java»
====

[Условия домашних заданий](http://www.kgeorgiy.info/courses/java-advanced/homeworks.html)

Домашнее задание 9. HelloUDP
----
* Протестировать клиент:

        info.kgeorgiy.java.advanced.hello.Tester client <полное имя класса>

* Протестировать сервер:

        info.kgeorgiy.java.advanced.hello.Tester server <полное имя класса>

Исходный код тестов:

* [Клиента](java/info/kgeorgiy/java/advanced/hello/HelloClientTest.java)
* [Сервера](java/info/kgeorgiy/java/advanced/hello/HelloServerTest.java)


Домашнее задание 8. Web Crawler
----
* *Модификация*.
    * Получить с сайта `https://e.lanbook.com` информацию о
    книгах, изданных за последние 5 лет.
    * Разделы:
        * Математика
        * Физика
        * Информатика
    * Пример ссылки:

        Алексеев, А.И. Сборник задач по классической электродинамике. 
        [Электронный ресурс] — Электрон. дан. — СПб. : Лань, 2008. — 320 с. — 
        Режим доступа: http://e.lanbook.com/book/100 — Загл. с экрана.

* Протестировать простую версию задания:

        info.kgeorgiy.java.advanced.crawler.Tester easy <полное имя класса>

* Протестировать сложную версию задания:

        info.kgeorgiy.java.advanced.crawler.Tester hard <полное имя класса>

Исходный код тестов:

* [Интерфейсы и вспомогательные классы](java/info/kgeorgiy/java/advanced/crawler/)
* [Простой вариант](java/info/kgeorgiy/java/advanced/crawler/CrawlerEasyTest.java)
* [Сложный вариант](java/info/kgeorgiy/java/advanced/crawler/CrawlerHardTest.java)


Домашнее задание 7. Параллельный запуск
----
* Протестировать простую версию задания:

        info.kgeorgiy.java.advanced.mapper.Tester scalar <ParallelMapperImpl>,<IterativeParallelism>

* Протестировать сложную версию задания:

        info.kgeorgiy.java.advanced.mapper.Tester list <ParallelMapperImpl>,<IterativeParallelism>

Внимание! Между полными именами классов `ParallelMapperImpl` и `IterativeParallelism` должна
быть запятая и не должно быть пробелов.

Исходный код тестов:

* [Простой вариант](java/info/kgeorgiy/java/advanced/mapper/ScalarMapperTest.java)
* [Сложный вариант](java/info/kgeorgiy/java/advanced/mapper/ListMapperTest.java)


Домашнее задание 6. Итеративный параллелизм
----
* Протестировать сложную версию задания:

        info.kgeorgiy.java.advanced.concurrent.Tester list <полное имя класса>

  Класс должен реализовывать интерфейс
  [ListIP](java/info/kgeorgiy/java/advanced/concurrent/ListIP.java).
* Протестировать простую версию задания:

        info.kgeorgiy.java.advanced.concurrent.Tester scalar <полное имя класса>

  Класс должен реализовывать интерфейс
  [ScalarIP](java/info/kgeorgiy/java/advanced/concurrent/ScalarIP.java).

Исходный код тестов:

* [Простой вариант](java/info/kgeorgiy/java/advanced/concurrent/ScalarIPTest.java)
* [Сложный вариант](java/info/kgeorgiy/java/advanced/concurrent/ListIPTest.java)

Домашнее задание 4. JarImplementor
----
Класс должен реализовывать интерфейс
[JarImpler](java/info/kgeorgiy/java/advanced/implementor/JarImpler.java).

* Протестировать простую версию задания:

        info.kgeorgiy.java.advanced.implementor.Tester jar-interface <полное имя класса>

* Протестировать сложную версию задания:

        info.kgeorgiy.java.advanced.implementor.Tester jar-class <полное имя класса>

Исходный код тестов:

* [Простой вариант](java/info/kgeorgiy/java/advanced/implementor/InterfaceJarImplementorTest.java)
* [Сложный вариант](java/info/kgeorgiy/java/advanced/implementor/ClassJarImplementorTest.java)


Домашнее задание 3. Implementor
----

Класс должен реализовывать интерфейс
[Impler](java/info/kgeorgiy/java/advanced/implementor/Impler.java).

* Протестировать простую версию задания:

        info.kgeorgiy.java.advanced.implementor.Tester interface <полное имя класса>

* Протестировать сложную версию задания:

        info.kgeorgiy.java.advanced.implementor.Tester class <полное имя класса>

  Класс должен реализовывать интерфейс
  [Impler](java/info/kgeorgiy/java/advanced/implementor/Impler.java).

Исходный код тестов:

* [Простой вариант](java/info/kgeorgiy/java/advanced/implementor/InterfaceImplementorTest.java)
* [Сложный вариант](java/info/kgeorgiy/java/advanced/implementor/ClassImplementorTest.java)

Домашнее задание 2. ArraySortedSet
----
* Протестировать сложную версию задания:

        info.kgeorgiy.java.advanced.arrayset.Tester NavigableSet <полное имя класса>

* Протестировать простую версию задания:

        info.kgeorgiy.java.advanced.arrayset.Tester SortedSet <полное имя класса>

Исходный код тестов:

* [Простой вариант](java/info/kgeorgiy/java/advanced/arrayset/SortedSetTest.java)
* [Сложный вариант](java/info/kgeorgiy/java/advanced/arrayset/NavigableSetTest.java)


Домашнее задание 1. Обход файлов
----
Для того, чтобы протестировать программу:

 1. Скачайте тесты ([WalkTest.jar](artifacts/WalkTest.jar)) и библиотеки к ним:
    [junit-4.11.jar](lib/junit-4.11.jar) [hamcrest-core-1.3.jar](lib/hamcrest-core-1.3.jar)
 * Откомпилируйте решение домашнего задания
 * Запустите

        info.kgeorgiy.java.advanced.walk.Tester Walk <полное имя класса>

   для простого варианта, и

        info.kgeorgiy.java.advanced.walk.Tester RecursiveWalk <полное имя класса>

   для сложного. Обратите внимание, что все скачанные `.jar` файлы должны
   быть указаны в `CLASSPATH`.

Исходный код тестов:

* [Простой вариант](java/info/kgeorgiy/java/advanced/walk/WalkTest.java)
* [Сложный вариант](java/info/kgeorgiy/java/advanced/walk/RecursiveWalkTest.java)
