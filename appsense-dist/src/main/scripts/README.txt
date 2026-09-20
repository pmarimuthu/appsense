AppSense — Log Viewer for JBoss EAP and WebLogic
=================================================

Requirements
------------
  Java 21 or later (JAVA_HOME set, or java/javaw on PATH)

Launchers
---------
  appsense.cmd        Swing GUI (double-click or run from cmd)
  appsense-cli.cmd    Full-screen terminal CLI

CLI usage
---------
  appsense-cli.cmd [<profile-name>] [<log-label>]

  If profile or log is omitted you will be prompted with a numbered list.

Configuration
-------------
  Profiles and settings are stored in %USERPROFILE%\.appsense\
  Run the GUI first to add server profiles.

Key bindings (CLI)
------------------
  Space        Pause / Resume
  /            Search  (Esc to clear)
  a / e / w    Level filter: All / Error / Warn
  d            Toggle since-last-deployment view
  x            Discard previous log lines (with confirm)
  m            Mark deployment now
  c            Copy selected summary row to clipboard
  Up / Down    Scroll log
  Enter        Expand / collapse stack trace
  q            Quit
