const webpack = require('webpack');
const path = require("path");
const HtmlWebpackPlugin = require('html-webpack-plugin');

const port = process.env.PORT || 3000;

module.exports = {
  mode: 'development',
  entry: './src/main/script/index.js',
  output: {
    path: path.resolve(__dirname, 'src/main/resources/static/built'),
    filename: 'bundle.js'
  },
  devtool: 'inline-source-map',
  module: {
    rules: [
      {
        test: /\.(js)$/,
        exclude: /node_modules/,
        use: ['babel-loader']
      },
      {
        test: /\.css$/,
        use: [
          {
            loader: 'style-loader'
          },
          {
            loader: 'css-loader',
            options: {
              modules: true,
              camelCase: true,
              sourceMap: true
            }
          }
        ]
      },
      {
        test: /\.less$/,
        use: [
          {
            loader: 'style-loader'
          },
          {
            loader: 'css-loader',
          },
          {
            loader: 'less-loader',
            options: {
              javascriptEnabled: true
            }
          }
        ]
      },
      {
        test:/\.(png|jpg|gif)$/ ,
        use:[{
          loader: 'url-loader',
          options: {
            limit: 8192,
            outputPath: './src/main/resources/static/images/',
          }
        }]
      },
    ]
  },
  plugins: [
    // new HtmlWebpackPlugin({
    //   template: './src/main/resources/template/index.html',
    //   favicon: './src/main/resources/static/favicon.ico'
    // })
  ],
  devServer: {
    host: '0.0.0.0',
    port: port,
    historyApiFallback: true,
    open: true
  }
};