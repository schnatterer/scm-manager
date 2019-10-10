const path = require("path");

module.exports = {
  entry: {
    "scm-legacy-plugin": "./src/main/js/index.js"
  },
  devtool: "source-map",
  target: "web",
  node: {
    fs: "empty",
    net: "empty",
    tls: "empty"
  },
  externals: [
    "react",
    "react-dom",
    // "react-jss",
    "react-i18next",
    "@scm-manager/ui-types",
    "@scm-manager/ui-extensions",
    "@scm-manager/ui-components"
  ],
  module: {
    rules: [
      {
        parser: {
          system: false,
          systemjs: false
        }
      },
      {
        test: /\.(js|jsx)$/,
        // exclude: /node_modules/,
        use: {
          loader: "babel-loader",
          options: {
            presets: ["@scm-manager/babel-preset"]
          }
        }
      },
      {
        test: /\.(css|scss|sass)$/i,
        use: [
          // Creates `style` nodes from JS strings
          "style-loader",
          // Translates CSS into CommonJS
          "css-loader",
          // Compiles Sass to CSS
          "sass-loader"
        ]
      },
      {
        test: /\.(png|svg|jpg|gif|woff2?|eot|ttf)$/,
        use: ["file-loader"]
      }
    ]
  },
  output: {
    path: path.join(__dirname, "target", "scm-legacy-plugin-2.0.0-SNAPSHOT", "webapp", "assets"),
    filename: "[name].bundle.js",
    library: "scm-legacy-plugin",
    libraryTarget: "amd"
  }
};
