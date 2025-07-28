# 💎 Jetpack Compose In-App Purchase (One-Time Purchase)

A clean and modular Android implementation of **Google Play Billing (one-time purchases)** using **Jetpack Compose**, **Kotlin Coroutines**, and **Koin**.

This repository aims to help Android developers integrate in-app billing with **Clean Architecture**, **ViewModel**, and **StateFlow** in a production-ready way.

---

## ✨ Features

- 🔄 One-Time Purchase Support  
- 🧠 MVVM + UseCase Pattern  
- 🧱 Clean architecture layers  
- 🔄 Billing connection lifecycle management  
- 📦 Dependency Injection with Koin  
- ⚡ Coroutine + Flow-based reactive API  
- 🛍 Query product details & purchases  


- **UseCases**: Encapsulate one billing responsibility each (e.g., start, end, purchase).
- **BillingManager**: Central handler that wraps `BillingClient`.
- **ViewModel**: Observes state and exposes data via `StateFlow`.

---

## 🛠️ Tech Stack

| Layer         | Library               |
|---------------|------------------------|
| UI            | Jetpack Compose        |
| State Mgmt    | Kotlin Flow, ViewModel |
| Billing       | Google Play Billing    |
| DI            | Koin                   |
| Arch Pattern  | MVVM + UseCases        |

---

## 📦 Modules / UseCases

- `StartBillingConnectionUseCase`
- `EndBillingConnectionUseCase`
- `PurchaseProductUseCase`
- `QueryProductDetailsUseCase`
- `QueryProductPurchasedUseCase`
- `ConsumeOneTimePurchaseUseCase`

---

## ✅ Setup

> Make sure to add the permission in `AndroidManifest.xml`:

```xml
<uses-permission android:name="com.android.vending.BILLING" />

👨‍💻 Contributing

Contributions, issues, and pull requests are welcome!
If you find this useful, give it a ⭐️ and share it with the community.


