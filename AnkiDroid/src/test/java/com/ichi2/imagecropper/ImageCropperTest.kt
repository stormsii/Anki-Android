/*
* This program is free software; you can redistribute it and/or modify it under
* the terms of the GNU General Public License as published by the Free Software
* Foundation; either version 3 of the License, or (at your option) any later
* version.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
* FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
* details.
*
* You should have received a copy of the GNU General Public License along with
* this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.ichi2.imagecropper

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ichi2.anki.RobolectricTest
import com.ichi2.testutils.AnkiFragmentScenario
import com.ichi2.testutils.launchFragment
import com.ichi2.utils.ContentResolverUtil
import com.ichi2.utils.openInputStreamSafe
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.reflect.Method
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@RunWith(AndroidJUnit4::class)
class ImageCropperTest : RobolectricTest() {

    private lateinit var scenario: AnkiFragmentScenario<ImageCropper>
    private lateinit var getImageCompressFormat: Method
    private lateinit var isImageTooBig: Method

    @Before
    override fun setUp() {
        super.setUp()
        mockkObject(ContentResolverUtil)
        mockkStatic("com.ichi2.utils.FileUtilKt")

        getImageCompressFormat =
            ImageCropper::class.java.getDeclaredMethod(
                "getImageCompressFormat",
                Uri::class.java,
            ).apply {
                isAccessible = true
            }

        isImageTooBig =
            ImageCropper::class.java.getDeclaredMethod(
                "isImageTooBig",
                Uri::class.java,
            ).apply {
                isAccessible = true
            }

        scenario = launchFragment(initialState = Lifecycle.State.CREATED)
    }

    @After
    fun tearDownMocks() {
        scenario.close()
        unmockkAll()
    }

    @Test
    fun imageURIFailure() {
        every { ContentResolverUtil.getFileName(any(), any()) } throws RuntimeException("bad uri")

        scenario.onFragment { fragment ->
            val format =
                getImageCompressFormat.invoke(
                    fragment,
                    Uri.parse("content://test/broken"),
                ) as Bitmap.CompressFormat

            assertEquals(Bitmap.CompressFormat.JPEG, format)
        }
    }

    @Test
    fun imageCropFailure() {
        every { ContentResolverUtil.getFileName(any(), any()) } returns "test.gif"

        scenario.onFragment { fragment ->
            val format =
                getImageCompressFormat.invoke(
                    fragment,
                    Uri.parse("content://test/test.gif"),
                ) as Bitmap.CompressFormat

            assertEquals(Bitmap.CompressFormat.JPEG, format)
        }
    }

    @Test
    fun imageTooBig() {
        every { any<ContentResolver>().openInputStreamSafe(any()) } returns null

        scenario.onFragment { fragment ->
            val result =
                isImageTooBig.invoke(
                    fragment,
                    Uri.parse("content://test/test.png"),
                ) as Boolean

            assertFalse(result)
        }
    }

    @Test
    fun pNGTestImage() {
        every { ContentResolverUtil.getFileName(any(), any()) } returns "test.png"

        scenario.onFragment { fragment ->
            val format =
                getImageCompressFormat.invoke(
                    fragment,
                    Uri.parse("content://test/test.png"),
                ) as Bitmap.CompressFormat

            assertEquals(Bitmap.CompressFormat.PNG, format)
        }
    }

    @Test
    fun jPGTestImage() {
        every { ContentResolverUtil.getFileName(any(), any()) } returns "test.jpg"

        scenario.onFragment { fragment ->
            val format =
                getImageCompressFormat.invoke(
                    fragment,
                    Uri.parse("content://test/test.jpg"),
                ) as Bitmap.CompressFormat

            assertEquals(Bitmap.CompressFormat.JPEG, format)
        }
    }

    @Test
    fun wEBPTestImage() {
        every { ContentResolverUtil.getFileName(any(), any()) } returns "test.webp"

        scenario.onFragment { fragment ->
            val format =
                getImageCompressFormat.invoke(
                    fragment,
                    Uri.parse("content://test/test.webp"),
                ) as Bitmap.CompressFormat

            val expected =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSLESS
                } else {
                    Bitmap.CompressFormat.WEBP
                }

            assertEquals(expected, format)
        }
    }

    @Test
    fun gifTestImage() {
        every { ContentResolverUtil.getFileName(any(), any()) } returns "test.gif"

        scenario.onFragment { fragment ->
            val format =
                getImageCompressFormat.invoke(
                    fragment,
                    Uri.parse("content://test/test.gif"),
                ) as Bitmap.CompressFormat

            assertEquals(Bitmap.CompressFormat.JPEG, format)
        }
    }
}