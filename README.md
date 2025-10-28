# MiChauchero

##  Aplicación de Tracking Financiero

MiChauchero es una aplicación móvil de finanzas personales que te permite gestionar tus ingresos y gastos de manera sencilla e intuitiva.  

##  Características

- Registro de Transacciones: Añade ingresos y gastos fácilmente
- Historial de Movimientos: Visualiza todas tus transacciones en un solo lugar
- Interfaz Intuitiva: Diseño moderno con Jetpack Compose

## Tecnologías Utilizadas

- Kotlin: Lenguaje de programación principal
- Jetpack Compose**: Framework moderno para UI
- MVVM: Patrón de arquitectura para separar la lógica de la interfaz
- Room Database**: Persistencia de datos local
- Coroutines & Flow**: Programación asíncrona y reactiva

## Uso

1. Añadir una transacción: Pulsa el botón "+" flotante y completa el formulario
2. Ver historial: Navega a la pestaña de transacciones para ver todos tus movimientos

## Arquitectura

La aplicación sigue el patrón de arquitectura MVVM (Model-View-ViewModel):

- **Model**: Entidades y repositorios que manejan los datos
- **View**: Componentes Compose que muestran la interfaz de usuario
- **ViewModel**: Clases que contienen la lógica de negocio y estado de la UI
