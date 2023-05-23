# APlusChat
In Project Level gradle

allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

In App Level gradle

dependencies {
	        implementation 'com.github.AuxanoWeb:APlusChat:v1.0.0'
	}

