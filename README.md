**Please note that the documentation and wiki are just being prepared, so if you run into some inconsistencies, please give it a few days.**

[![Release](https://jitpack.io/v/daemontus/glucose.svg)](https://jitpack.io/#daemontus/glucose)
[![Build Status](https://travis-ci.org/daemontus/glucose.svg?branch=tests)](https://travis-ci.org/daemontus/glucose)
[![codecov.io](https://codecov.io/github/daemontus/glucose/coverage.svg?branch=master)](https://codecov.io/github/daemontus/glucose?branch=master)
[![Methods Count](https://img.shields.io/badge/Methods and size-core: 634 | 107 KB-e91e63.svg)](http://www.methodscount.com/?lib=com.github.daemontus%3Aglucose%3A0.0.9)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=flat)](https://github.com/daemontus/glucose/blob/master/LICENSE)

![Logo](static/logo.jpg)


Glucose is a lightweight modular replacement for Android Fragments written in **Kotlin** and designed with **simplicity**, **composability**, **safety** and **reactive** programming in mind. It does not enforce any specific MV\* paradigm. Instead, it serves as a clean and reliable **lifecycle provider** with focus on **safe asynchronous operations**. Finally, Glucose highly values **compatibility** with existing libraries and legacy code.

Main features of Glucose include:
 - A **Presenter** and a **PresenterGroup** components which replace Fragments.
 - A reactive **ActionHost** which replaces Fragment Transactions and other asychronous actions.
 - Strict and transparent lifecycle semantics with synchronous and asynchronous notifications.
 - More transparent and automated state preservation.
 - Caching of commonly used Presenters to avoid repeated layout inflation.
 - Granular configuration change management (per Presenter).

To learn more about Glucose, head to one of the wiki articles that explain the motivation, design and implementation of the library:
 - [**Motivation**](https://github.com/daemontus/glucose/wiki/Motivation): The problems that Glucose is trying to solve and how it relates to other libraries.
 - **Design**: The description of the library architecture and how it relates to the problems from motivation.
 - **Implementation**: Code examples and detailed walkthrough of the library API.

Alternatively, you can just skim this FAQ:

### How to include Glucose into your project?
Glucose currently lives on [Jitpack](https://jitpack.io/#daemontus/glucose). There is a possiblity to start publishing stable artifacts to maven central later on, when Glucose moves out of beta, but for now, you will have to use Jitpack.

The library itself is devided into modules (to reduce the amount of unnecessary methods and dependencies):

	ext.glucose_version = 1.0.0-alpha1
	dependencies {
		//Main module that includes the Presenter and other essential components
		compile "com.github.daemontus.glucose:core:$glucose_version"
		//A module for interop with Support Library (RootCompatActivity, FragmentPresenter etc.)
		compile "com.github.daemontus.glucose:compat:$glucose_version"
		//A module with helper functions for bundling arrays
	    compile "com.github.daemontus.glucose:bundle-array:$glucose_version"
	    //A module with helper functions for bundling lists
	    compile "com.github.daemontus.glucose:bundle-list:$glucose_version"
	}

### Why is it in beta? Is it ready for production?
Thousands of users already use apps that contain older versions of the code in this repository. However, it hasn't been published anywyhere else before and it has been through a major refactoring recently. Therefore it is currently marked as beta to indicate that some minor bugfixes or API updates might still be necessary.

However, the whole project is covered by JUnit and Robolectric tests, with the total coverage of over 90% (most of the missing stuff are Kotlin inline methods) and should be well tested and stable. 

**So yes, Glucose is ready for production, with a few possible minor changes in the future.**

### I am not using Kotlin (yet), can I use Glucose?
Yes, Kotlin has a great interoperability with existing Java code. A few specific features (such as simplified syntax for creating bundles) might be a little cumbersome to use, but the rest of the API is pretty much language agnostic.

### Can I use Android Fragments and Glucose Presenters in one App/Module/Activity/Room/Conversation?
Glucose highly values interoperability with existing libraries. To this end, it provides two interop components: **FragmentPresenter** and **PresenterFragment**. First one allows you to safely include a specific Fragment class into the Presenter tree. The latter then allows you to plant a Presenter tree into a Fragment.

Of course, a Presenter hosted in a Fragment will inherit some fragment-related problems (example: If the fragment doesn't receive notification about activity result, neither does the hosted Presenter), but all major functionality should work as expected.

### Exactly how lightweight is it?
The core module has only ~650 methods and 110kB. The only major dependencies are RxJava and Kotlin Standard Library (both of which you are probably using anyway). Apart from these, there is only RxAndroid, SupportAnnotations and a small library for Option and Result types. You don't even have to use the Android Support Library. 

In terms of speed, there aren't any specific benchmarks available now. However, complex layouts should benefit from Presenter caching (assuming sufficient memory is available) and the action mechanism promotes asynchronous coding style which should reduce the load on the main thread if used correctly :)

### Uhm, actually... XXX could be better.
If you have some concrete suggestion, you are wellcome to submit a pull request or create an issue. If you have a feature request that doesn't have to be integrated into some existing module, the best option would be to publish a separate repository which can be then mentioned in the module list above. This way you retain the ownership of the code and the base library stays as light as possible. (If you publish such repository, create an issue with a link to it, or make a pull request with a modified readme)

### What's next?
Currently, there are several planned extensions (mostly implemented as separate modules):
 - A BackStack implementation that detaches presenters as soon as they are not visible and handles state restoration properly.
 - A ResourceDelegate, that will react to configuration changes and reload resources in case of a configuration change.
 - A way to present Dialogs and Snackbars.
 - A set of utility PresenterGroups - for proper remote data fetching, for morphing between two presenters, etc.
 - A module that simplifies animations involving tranfer of presenters between two positions in the main tree. 
 - Lint checker for basic problems, like accessing arguments in constructor (before onAttach), uncommited actions, etc.
