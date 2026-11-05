# Third-party notices

The project MIT license covers original Kinegra Home code, documentation and original artwork. It does not relicense third-party code, Android/NVIDIA/SHIELD marks, third-party app artwork or the NVIDIA website content visible in documentation screenshots. Screenshot attribution is recorded in [the media notes](assets/readme/README.md).

## AdbLib cryptography

Only `AdbCrypto` and `AdbBase64` from Cameron Gutman's AdbLib are compiled. Source revision: `d6937951eb98557c76ee2081e383d50886ce109a`. The upstream BSD 3-Clause terms are retained below and in [the vendored license](third_party/adblib/LICENSE). Kinegra Home supplies its own loopback transport; see [the source notes](third_party/adblib/README.md).

When distributing an APK, include this notice and the project license with the release materials. The Android platform and SDK are build/system dependencies; the compile-only framework declarations are not shipped as Android replacement classes.

```text
Copyright (c) 2013, Cameron Gutman
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

  Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

  Redistributions in binary form must reproduce the above copyright notice, this
  list of conditions and the following disclaimer in the documentation and/or
  other materials provided with the distribution.

  Neither the name of the {organization} nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
```
