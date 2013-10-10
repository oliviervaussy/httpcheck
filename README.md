# http-check

Test an HTTP interface according to a simple .json file.

- checks return code
- checks body

Run a test suite against an HTTP API from a .json file.

## Usage

```
http-check path/to/config.json
```

## File format

```json
{
  "vars": {
    "scheme": "https",
    "host": "api.github.com",
    "headers": {}
  },
  "tests": [{
    "name": "GitHub Public API",
    "headers": "headers",
    "paths": [
      {
        "path": "GET /nukes",
        "code": 404
      },
      {
        "path": "GET /users/blandinw",
        "assert": "(= \"blandinw\" (:login json))",
        "code": 200
      }
    ]
  }]
}
```

## License

MIT

Copyright © 2013 Willy Blandin

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
