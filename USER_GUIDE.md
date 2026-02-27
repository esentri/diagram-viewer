# DLC Diagram Viewer User Guide
This User Guide describes how to use the DLC Diagram Viewer. 
It is structured along the steps all the user needs to create a new diagram from scratch.
So, if you follow the steps in the given order, it works like a tutorial. 
For the basic setup, just follow the steps A) to D).

## A) Register User

1. Create an Account

![Register](./images/register.png)
 - Click on ``Create Account``.

2. Fill in the form
![Create Account](./images/create_account.png)
 - There is currently no email validation, so be sure there is no typo in your email adress 
 - There is currently no password reset, so be sure you do not forget your password

Click on ``Register``

## B) Login
![Login](./images/login.png)
- Enter your email adress form registration as username and password
- Click on ``Log in``

## C) Upload Domain Model

1. Get your API Key
- After login, you can open the user menu (circle in upper right corner)
![After Login](./images/after_login_empty.png)
- Click on ``Copy API-Key``
![API Keys](./images/copy_API_key.png)
- Use the API Key with the build plugin as described below 

2. Upload your domain model via build plugin
- Use one of the [DLC Build Plugins](https://github.com/esentri/domainlifecycles/tree/main/dlc-plugins) 
to upload your domain model to the DLC Diagram Viewer. There you may also find a usage description for the Maven plugin.
- You can test the upload by checking out the [Demo Project](https://github.com/esentri/ddd-hotel-demo)

```Gradle
plugins {
	id 'io.domainlifecycles.dlc-gradle-plugin' version '3.0.0'
}
...
dlcGradlePlugin {
    domainModelUpload {
        domainModelPackages = ["com.esentri"]
        projectName = "ddd-reception-demo"
        apiKey = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
        diagramViewerBaseUrl = "http://localhost:8090"
    }
}
```
- Change the parameters to your needs
- Don't forget to have a DLC diagram Viewer running, when executing the plugin
- Insert the API key from the DLC Diagram Viewer into the build plugin parameters

3. After a successful upload, refresh the diagram viewer page and you can see the new project on the left side.
![Uploaded](./images/uploaded.png)

## D) Add New Diagram

1. Click on the project first
2. Then click ``Create new Diagram``
![Create new diagram](./images/create_new_diagram.png)
3. Fill in the Diagram Name and click ``Create``
![Create dialog](./images/create_dialog.png)
4. After creating the diagram, you can see the diagram name in the menu on the left side. If you click there, you can see the diagram.
![Diagram created](./images/diagram_created.png)

## E) Share Project

1. Click on the project on the left side, then click on ``Share Project``
![Share Project](./images/share_project.png)
2. Click on ``Add user`` in the following dialog
3. Enter the email adress of the user you want to share the project with.
The user must have registered with the diagram viewer before!
4. If that user now logs in, he can see the project on the left side and all the corresponding diagrams.
He can also edit the diagrams and create new ones, which are automatically shared amon all project users.

## F) Analyzing The Model
For a large project containing a large domain model one diagram with all details my be overwhelming.
There are several options to filter the model and only show the relevant parts in new diagrams.

### Filter on package level
We have structured our demo project according to Ports&Adapters, so there is a package 
``com.esentri.rezeption.inbound``.

1. On the right side, add the package in tzhe field ``Excplicitly included packages``, then the diagram will only show the 
model elements from this package and its subpackages.
![Explicitly included packages](./images/explicitly_included_packages.png)

### General visibility on stereotype level

1. Left to the current diagram, there is a button with a small eye icon. This opens the general visibility settings of the diagram.
![General visibility](./images/general_visibility.png)
There you define, if fields or methods are shown, if inheritance structures are relevant in the diagram.
Additionally, you can define which stereotypes are shown.
2. For example, you can create a diagram showing only the Aggregates in the domain model, without showing field and method details.
![Aggregates only](./images/aggregates_only.png)

### Filter on the domain model element level
Sometimes it is useful to only show specific classes in the diagram and package level filtering is not a sufficient way.
In this case you can hide concrete domain model elements. 

1. On the right side open `Àdvanced view filters` 
2. Select the name of the building blocks to be hidden in ``Invisible objects``.

### Filter on connections / relations
Among the advanced filters there are also filters for connections/relations:
There are 4 types of connection based filters:
- ``Include connections to``: Show all elements connected somehow to the specificed element
- ``Include ingoing connections to``: Show all elements having an ingoing connection to the specificed element
- ``Include outgoing connections from``: Show all elements having an outgoing connection from the specificed element
- ``Exclude ingoing connections to``: Hide all elements having an ingoing connection to the specificed element
- ``Exclude outgoing connections from``: Hide all elements having an outgoing connection from the specificed element

For example, to show the model elements, that are connected with the use cases implemented in ``ServiceLeistungenUseCases``,
one might enter the ApplicationService class ``ServiceLeistungUseCases`` in ``Include outgoing connections from`` and see:
![user_case_filter](./images/use_case_filter.png)

## G) Add Notes
There is the option to add notes to the diagram and add information, for discussion or documentation purposes. 

1. When a diagram is selected, on the right side click on ``Notes``.
2. Click ``Add / edit``
3. Select a type (model element) and add a note
4. Click the check mark to save the note
   ![note added](./images/note_added.png)

## Use templates to create new diagrams
When creating new diagrams you can use exisitng diagram as templates. All settings and filters will be copied and you can refine the new diagram with less clicks, 
if it's similar to the existing one.

1. Do the same steps as described above to create a new diagram.
2. Before saving the diagram choose ``Template`` in the advanced configuration and select the template you want to use.
   ![template](./images/template.png)


## H) Layout options
Left to the current diagram, there is a button with a small gears icon. 
This opens the general layout and fonts settings of the diagram.

![Layout settings](./images/layout_settings.png)

The default layout is a simple top-down layout (``Down``). 
But in some case left to right is better (``Right`)

![LExample left right](./images/example_left_right.png)

## I) Styling Options
Left to the current diagram, there is a button with a small paintbrush icon.
This opens the general style settings for the node types (supported DDD stereotypes) of the diagram.
Colors and other style settings of each node type can be changed.

## J) Download diagrams as images

When a diagram is selected, there is a Download button right above. SVG, PBG and JPEG exports are supported.
![Diagram created](./images/diagram_created.png)









